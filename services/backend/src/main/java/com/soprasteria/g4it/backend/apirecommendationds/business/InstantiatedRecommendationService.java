/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds.business;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.business.OutPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutVirtualEquipmentService;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialService;
import com.soprasteria.g4it.backend.apirecommendationds.mapper.RecommendationMapper;
import com.soprasteria.g4it.backend.apirecommendationds.modeldb.Recommendation;
import com.soprasteria.g4it.backend.server.gen.api.dto.InstantiatedRecommendationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service computing prioritized recommendations for a digital service version,
 * using the TOPSIS multi-criteria decision method.
 *
 * Criteria and weights:
 *   proportion  (0.40) -- share of category in total CLIMATE_CHANGE emissions -- beneficial
 *   heuristic   (0.30) -- distance of actual attribute value from ideal range -- beneficial
 *   baseImpact  (0.10) -- 0=LOW, 1=MEDIUM, 2=HIGH -- beneficial
 *   difficulty  (0.20) -- 1=EASY, 2=MEDIUM, 3=HARD -- cost (lower is better)
 */
@Service
@AllArgsConstructor
@Slf4j
public class InstantiatedRecommendationService {

    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;
    private final OutPhysicalEquipmentService outPhysicalEquipmentService;
    private final OutVirtualEquipmentService outVirtualEquipmentService;
    private final InDatacenterRepository inDatacenterRepository;
    private final InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    private final InVirtualEquipmentRepository inVirtualEquipmentRepository;
    private final ReferentialService referentialService;
    private final ObjectMapper objectMapper;

    // Criterion weights (must sum to 1.0)
    private static final double WEIGHT_PROPORTION = 0.40;
    private static final double WEIGHT_HEURISTIC  = 0.30;
    private static final double WEIGHT_IMPACT     = 0.10;
    private static final double WEIGHT_DIFFICULTY = 0.20;

    // Difficulty mapping
    private static final int DIFFICULTY_EASY   = 1;
    private static final int DIFFICULTY_MEDIUM = 2;
    private static final int DIFFICULTY_HARD   = 3;

    // Criterion for emission filtering
    private static final String CLIMATE_CHANGE = "CLIMATE_CHANGE";

    // Terminal equipment type
    private static final String TERMINAL_TYPE = "Terminal";

    /**
     * Returns recommendations sorted by descending TOPSIS priority score.
     */
    public List<InstantiatedRecommendationRest> getInstantiatedRecommendations(
            final String digitalServiceVersionUid,
            final Long organisationId) {

        List<Recommendation> recommendations = getAllRecommendationsForOrganisation(organisationId);

        if (recommendations.isEmpty()) {
            log.warn("TOPSIS: no recommendations found for organisationId={}", organisationId);
            return List.of();
        }

        // Compute emission proportions per category
        Map<String, Double> categoryProportions = computeCategoryProportions(digitalServiceVersionUid);
        log.info("TOPSIS: category proportions for dsVersionUid={}: {}", digitalServiceVersionUid, categoryProportions);

        // Compute heuristic context once (shared across recommendations)
        HeuristicContext heuristicContext = buildHeuristicContext(digitalServiceVersionUid);
        log.info("TOPSIS: heuristic context for dsVersionUid={}: avgPue={}, avgWorkload={}, avgDurationHour={}, majorityLocation={}",
                digitalServiceVersionUid,
                heuristicContext.avgPue,
                heuristicContext.avgWorkload,
                heuristicContext.avgDurationHour,
                heuristicContext.majorityLocation);

        // Build numeric matrix [n x 4]
        int n = recommendations.size();
        double[] impacts      = new double[n];
        double[] difficulties = new double[n];
        double[] proportions  = new double[n];
        double[] heuristics   = new double[n];

        for (int i = 0; i < n; i++) {
            Recommendation r = recommendations.get(i);
            impacts[i]      = r.getBaseImpact() != null ? r.getBaseImpact() : 0;
            difficulties[i] = mapDifficulty(r.getDifficulty());
            proportions[i]  = computeMaxProportion(r.getCategory(), categoryProportions);
            heuristics[i]   = computeHeuristicScore(r, heuristicContext);
        }

        // Run TOPSIS
        double[] priorities = topsis(proportions, heuristics, impacts, difficulties, n);

        // Build result DTOs
        List<InstantiatedRecommendationRest> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            RecommendationDSRest dto = recommendationMapper.toRest(recommendations.get(i));
            InstantiatedRecommendationRest instantiated = InstantiatedRecommendationRest.builder()
                    .idRecommendation(recommendations.get(i).getIdRecommendation())
                    .digitalServiceVersionUid(digitalServiceVersionUid)
                    .priority(priorities[i])
                    .recommendation(dto)
                    .build();
            result.add(instantiated);
        }

        result.sort(Comparator.comparingDouble(InstantiatedRecommendationRest::getPriority).reversed());

        log.info("TOPSIS: computed priorities for {} recommendations (dsVersionUid={})", n, digitalServiceVersionUid);

        return result;
    }

    // -------------------------------------------------------------------------
    // Heuristic context
    // -------------------------------------------------------------------------

    /**
     * Holds pre-computed actual values from the digital service version inputs.
     */
    private record HeuristicContext(
            Double avgPue,
            Double avgWorkload,
            Double avgDurationHour,
            String majorityLocation
    ) {}

    /**
     * Builds the heuristic context by reading actual input values for the digital service version.
     */
    private HeuristicContext buildHeuristicContext(String digitalServiceVersionUid) {
        // Average PUE from datacenters
        Double avgPue = inDatacenterRepository
                .findByDigitalServiceVersionUid(digitalServiceVersionUid)
                .stream()
                .filter(dc -> dc.getPue() != null)
                .mapToDouble(dc -> dc.getPue())
                .average()
                .orElse(Double.NaN);

        // Average workload from virtual equipments
        Double avgWorkload = inVirtualEquipmentRepository
                .findByDigitalServiceVersionUid(digitalServiceVersionUid)
                .stream()
                .filter(ve -> ve.getWorkload() != null)
                .mapToDouble(ve -> ve.getWorkload())
                .average()
                .orElse(Double.NaN);

        // Average durationHour from terminal physical equipments
        Double avgDurationHour = inPhysicalEquipmentRepository
                .findByDigitalServiceVersionUid(digitalServiceVersionUid)
                .stream()
                .filter(pe -> TERMINAL_TYPE.equals(pe.getType()) && pe.getDurationHour() != null)
                .mapToDouble(pe -> pe.getDurationHour())
                .average()
                .orElse(Double.NaN);

        // Majority location from virtual equipments + datacenters
        String majorityLocation = computeMajorityLocation(digitalServiceVersionUid);

        return new HeuristicContext(avgPue, avgWorkload, avgDurationHour, majorityLocation);
    }

    /**
     * Finds the most frequent non-null location across virtual equipments and datacenters.
     */
    private String computeMajorityLocation(String digitalServiceVersionUid) {
        Map<String, Long> locationCounts = new HashMap<>();

        inVirtualEquipmentRepository
                .findByDigitalServiceVersionUid(digitalServiceVersionUid)
                .stream()
                .filter(ve -> ve.getLocation() != null && !ve.getLocation().isBlank())
                .forEach(ve -> locationCounts.merge(ve.getLocation(), 1L, Long::sum));

        inDatacenterRepository
                .findByDigitalServiceVersionUid(digitalServiceVersionUid)
                .stream()
                .filter(dc -> dc.getLocation() != null && !dc.getLocation().isBlank())
                .forEach(dc -> locationCounts.merge(dc.getLocation(), 1L, Long::sum));

        return locationCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Heuristic score computation
    // -------------------------------------------------------------------------

    /**
     * Computes the heuristic score [0, 1] for a recommendation.
     *
     * Score = 0   -> actual value is in the best group (no action needed)
     * Score = 1   -> actual value is in the worst group (highly prioritize)
     * Score = 0.5 -> neutral (no affectedAttribute, or value unavailable)
     */
    private double computeHeuristicScore(Recommendation recommendation, HeuristicContext ctx) {
        String attribute = parseAffectedAttribute(recommendation.getAffectedAttributes());

        if (attribute == null) {
            return 0.5; // neutral
        }

        // location is computed dynamically via electricity mix quartiles, no heuristicRange needed
        if ("location".equals(attribute)) {
            return computeLocationHeuristic(ctx.majorityLocation);
        }

        if (recommendation.getHeuristicRange() == null) {
            return 0.5; // neutral -- no range defined for this attribute
        }

        return switch (attribute) {
            case "pue"                    -> computeNumericHeuristic(ctx.avgPue,          recommendation.getHeuristicRange());
            case "workload"               -> computeNumericHeuristic(ctx.avgWorkload,      recommendation.getHeuristicRange());
            case "yearlyUsageTimePerUser" -> computeNumericHeuristic(ctx.avgDurationHour,  recommendation.getHeuristicRange());
            default -> {
                log.warn("TOPSIS: unknown affectedAttribute '{}', using neutral score", attribute);
                yield 0.5;
            }
        };
    }

    /**
     * Computes heuristic score for a numeric attribute using heuristicRange JSON.
     *
     * heuristicRange format: {"1":[min,max],"2":[min,max],...}
     * Groups are ordered by priority: group "1" = best (score 0), last group = worst (score 1).
     * If the value is NaN (no data), returns neutral 0.5.
     */
    private double computeNumericHeuristic(Double actualValue, String heuristicRangeJson) {
        if (actualValue == null || Double.isNaN(actualValue)) return 0.5;

        try {
            Map<String, List<Double>> ranges = objectMapper.readValue(
                    heuristicRangeJson, new TypeReference<Map<String, List<Double>>>() {});

            // Find which group the actual value falls into
            int matchedIndex = -1;
            int totalGroups = ranges.size();

            // Sort keys numerically
            List<String> sortedKeys = ranges.keySet().stream()
                    .sorted(Comparator.comparingInt(Integer::parseInt))
                    .toList();

            for (int i = 0; i < sortedKeys.size(); i++) {
                List<Double> bounds = ranges.get(sortedKeys.get(i));
                if (bounds.size() < 2) continue;
                double min = bounds.get(0);
                double max = bounds.get(1);
                if (actualValue >= min && actualValue <= max) {
                    matchedIndex = i;
                    break;
                }
            }

            if (matchedIndex == -1) {
                // Value outside all ranges: assign worst score
                log.warn("TOPSIS: value {} not found in any heuristic range, assigning worst score", actualValue);
                return 1.0;
            }

            if (totalGroups == 1) return 0.0;

            // Score = index / (totalGroups - 1): group 0 -> 0.0, last group -> 1.0
            return (double) matchedIndex / (totalGroups - 1);

        } catch (Exception e) {
            log.error("TOPSIS: failed to parse heuristicRange JSON: {}", heuristicRangeJson, e);
            return 0.5;
        }
    }

    /**
     * Computes heuristic score for location using electricity mix quartiles.
     *
     * Quartile 1 (best mix) -> score 0.0 (not prioritized)
     * Quartile 4 (worst mix) -> score 1.0 (highly prioritized)
     * Unknown location -> neutral 0.5
     */
    private double computeLocationHeuristic(String location) {
        if (location == null || location.isBlank()) return 0.5;

        try {
            Map<Pair<String, String>, Integer> quartiles = referentialService.getElectricityMixQuartiles();
            Integer quartile = quartiles.get(Pair.of(location, CLIMATE_CHANGE));

            if (quartile == null) {
                log.warn("TOPSIS: no electricity mix quartile found for location={}, using neutral score", location);
                return 0.5;
            }

            double score = (quartile - 1.0) / 3.0;
            log.info("TOPSIS: location={} -> quartile={} -> heuristic score={}", location, quartile, score);
            return score;

        } catch (Exception e) {
            log.error("TOPSIS: failed to compute location heuristic for location={}", location, e);
            return 0.5;
        }
    }

    /**
     * Parses the affectedAttributes JSON string to extract the single attribute name.
     * Expected format: "\"pue\"" or "\"location\"" etc.
     */
    private String parseAffectedAttribute(String affectedAttributesJson) {
        if (affectedAttributesJson == null || affectedAttributesJson.isBlank()) return null;
        try {
            return objectMapper.readValue(affectedAttributesJson, String.class);
        } catch (Exception e) {
            log.error("TOPSIS: failed to parse affectedAttributes JSON: {}", affectedAttributesJson, e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Emission proportion computation
    // -------------------------------------------------------------------------

    private Map<String, Double> computeCategoryProportions(String digitalServiceVersionUid) {
        Map<String, Double> emissions = new HashMap<>();
        emissions.put("TERMINAL",               0.0);
        emissions.put("NETWORK",                0.0);
        emissions.put("PRIVATE_INFRASTRUCTURE", 0.0);
        emissions.put("PUBLIC_CLOUD",           0.0);

        List<OutPhysicalEquipmentRest> physicalEquipments =
                outPhysicalEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid);

        log.info("TOPSIS: physical equipments count={}, equipmentTypes={}",
                physicalEquipments.size(),
                physicalEquipments.stream().map(OutPhysicalEquipmentRest::getEquipmentType).distinct().toList());

        for (OutPhysicalEquipmentRest eq : physicalEquipments) {
            if (!CLIMATE_CHANGE.equals(eq.getCriterion())) continue;
            if (eq.getUnitImpact() == null) continue;
            String category = mapPhysicalEquipmentTypeToCategory(eq.getEquipmentType());
            if (category != null) emissions.merge(category, eq.getUnitImpact(), Double::sum);
        }

        List<OutVirtualEquipmentRest> virtualEquipments =
                outVirtualEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid);

        log.info("TOPSIS: virtual equipments count={}, infrastructureTypes={}",
                virtualEquipments.size(),
                virtualEquipments.stream().map(OutVirtualEquipmentRest::getInfrastructureType).distinct().toList());

        for (OutVirtualEquipmentRest eq : virtualEquipments) {
            if (!CLIMATE_CHANGE.equals(eq.getCriterion())) continue;
            if (eq.getUnitImpact() == null) continue;
            String category = mapVirtualEquipmentTypeToCategory(eq.getInfrastructureType());
            if (category != null) emissions.merge(category, eq.getUnitImpact(), Double::sum);
        }

        double total = emissions.values().stream().mapToDouble(Double::doubleValue).sum();

        if (total == 0) {
            log.warn("TOPSIS: no CLIMATE_CHANGE emissions found for dsVersionUid={}, proportions default to 0", digitalServiceVersionUid);
            return emissions;
        }

        Map<String, Double> proportions = new HashMap<>();
        emissions.forEach((cat, impact) -> proportions.put(cat, impact / total));
        return proportions;
    }

    private double computeMaxProportion(List<String> categories, Map<String, Double> proportions) {
        if (categories == null || categories.isEmpty()) return 0.0;
        return categories.stream()
                .mapToDouble(cat -> proportions.getOrDefault(cat, 0.0))
                .max()
                .orElse(0.0);
    }

    private String mapPhysicalEquipmentTypeToCategory(String equipmentType) {
        if (equipmentType == null) return null;
        return switch (equipmentType) {
            case "Terminal"         -> "TERMINAL";
            case "Network"          -> "NETWORK";
            case "Server",
                 "Dedicated Server" -> "PRIVATE_INFRASTRUCTURE";
            default                 -> null;
        };
    }

    private String mapVirtualEquipmentTypeToCategory(String infrastructureType) {
        if (infrastructureType == null) return null;
        return switch (infrastructureType) {
            case "Cloud",
                 "CLOUD_SERVICES" -> "PUBLIC_CLOUD";
            default              -> null;
        };
    }

    // -------------------------------------------------------------------------
    // TOPSIS implementation
    // -------------------------------------------------------------------------

    /**
     * Computes TOPSIS proximity scores for n alternatives with 4 criteria.
     *
     * Criterion 1 -- proportion : beneficial (higher = more impactful category)
     * Criterion 2 -- heuristic  : beneficial (higher = further from ideal, more urgent)
     * Criterion 3 -- baseImpact : beneficial (higher raw value = better)
     * Criterion 4 -- difficulty : cost       (lower  raw value = better)
     */
    private double[] topsis(double[] proportions, double[] heuristics, double[] impacts, double[] difficulties, int n) {

        // Step 1 -- Euclidean normalisation per column
        double normProportion = euclideanNorm(proportions, n);
        double normHeuristic  = euclideanNorm(heuristics,  n);
        double normImpact     = euclideanNorm(impacts,     n);
        double normDifficulty = euclideanNorm(difficulties, n);

        double[] rProportion = new double[n];
        double[] rHeuristic  = new double[n];
        double[] rImpact     = new double[n];
        double[] rDifficulty = new double[n];

        for (int i = 0; i < n; i++) {
            rProportion[i] = normProportion > 0 ? proportions[i]  / normProportion : 0;
            rHeuristic[i]  = normHeuristic  > 0 ? heuristics[i]   / normHeuristic  : 0;
            rImpact[i]     = normImpact     > 0 ? impacts[i]       / normImpact     : 0;
            rDifficulty[i] = normDifficulty > 0 ? difficulties[i]  / normDifficulty : 0;
        }

        // Step 2 -- Apply weights
        double[] vProportion = new double[n];
        double[] vHeuristic  = new double[n];
        double[] vImpact     = new double[n];
        double[] vDifficulty = new double[n];

        for (int i = 0; i < n; i++) {
            vProportion[i] = rProportion[i] * WEIGHT_PROPORTION;
            vHeuristic[i]  = rHeuristic[i]  * WEIGHT_HEURISTIC;
            vImpact[i]     = rImpact[i]      * WEIGHT_IMPACT;
            vDifficulty[i] = rDifficulty[i]  * WEIGHT_DIFFICULTY;
        }

        // Step 3 -- Ideal best (A+) and worst (A-)
        //   proportion -> beneficial -> A+ = max, A- = min
        //   heuristic  -> beneficial -> A+ = max, A- = min
        //   impact     -> beneficial -> A+ = max, A- = min
        //   difficulty -> cost       -> A+ = min, A- = max
        double aPlusProportion  = max(vProportion, n);
        double aMinusProportion = min(vProportion, n);
        double aPlusHeuristic   = max(vHeuristic,  n);
        double aMinusHeuristic  = min(vHeuristic,  n);
        double aPlusImpact      = max(vImpact,     n);
        double aMinusImpact     = min(vImpact,     n);
        double aPlusDifficulty  = min(vDifficulty, n);
        double aMinusDifficulty = max(vDifficulty, n);

        // Step 4 -- Euclidean distances to A+ and A-
        double[] dPlus  = new double[n];
        double[] dMinus = new double[n];

        for (int i = 0; i < n; i++) {
            dPlus[i] = Math.sqrt(
                    sq(vProportion[i] - aPlusProportion) +
                    sq(vHeuristic[i]  - aPlusHeuristic)  +
                    sq(vImpact[i]     - aPlusImpact)     +
                    sq(vDifficulty[i] - aPlusDifficulty)
            );
            dMinus[i] = Math.sqrt(
                    sq(vProportion[i] - aMinusProportion) +
                    sq(vHeuristic[i]  - aMinusHeuristic)  +
                    sq(vImpact[i]     - aMinusImpact)     +
                    sq(vDifficulty[i] - aMinusDifficulty)
            );
        }

        // Step 5 -- Proximity score S* = d- / (d- + d+)
        double[] scores = new double[n];
        for (int i = 0; i < n; i++) {
            double denom = dMinus[i] + dPlus[i];
            scores[i] = denom > 0 ? dMinus[i] / denom : 0.5;
        }

        return scores;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private double euclideanNorm(double[] v, int n) {
        double sum = 0;
        for (int i = 0; i < n; i++) sum += v[i] * v[i];
        return Math.sqrt(sum);
    }

    private double max(double[] v, int n) {
        double m = v[0];
        for (int i = 1; i < n; i++) if (v[i] > m) m = v[i];
        return m;
    }

    private double min(double[] v, int n) {
        double m = v[0];
        for (int i = 1; i < n; i++) if (v[i] < m) m = v[i];
        return m;
    }

    private double sq(double x) { return x * x; }

    private double mapDifficulty(String difficulty) {
        if (difficulty == null) return DIFFICULTY_MEDIUM;
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> DIFFICULTY_EASY;
            case "HARD" -> DIFFICULTY_HARD;
            default     -> DIFFICULTY_MEDIUM;
        };
    }

    private List<Recommendation> getAllRecommendationsForOrganisation(Long organisationId) {
        List<Recommendation> all = new ArrayList<>();
        all.addAll(recommendationService.findByOrganisationIdIsNull());
        all.addAll(recommendationService.findByOrganisationId(organisationId));
        return all;
    }
}