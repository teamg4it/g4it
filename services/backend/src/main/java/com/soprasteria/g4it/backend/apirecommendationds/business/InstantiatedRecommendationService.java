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
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
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

    // Physical equipment types
    private static final String TYPE_TERMINAL          = "Terminal";
    private static final String TYPE_NETWORK           = "Network";
    private static final String TYPE_SERVER            = "Server";
    private static final String TYPE_DEDICATED_SERVER  = "Dedicated Server";

    // Virtual equipment infrastructure types
    private static final String INFRA_CLOUD            = "Cloud";
    private static final String INFRA_CLOUD_SERVICES   = "CLOUD_SERVICES";

    // TOPSIS emission categories
    private static final String CATEGORY_TERMINAL              = "TERMINAL";
    private static final String CATEGORY_NETWORK               = "NETWORK";
    private static final String CATEGORY_PRIVATE_INFRASTRUCTURE = "PRIVATE_INFRASTRUCTURE";
    private static final String CATEGORY_PUBLIC_CLOUD          = "PUBLIC_CLOUD";

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
        log.info("TOPSIS: heuristic context for dsVersionUid={}: weightedAverages={}, locations={}",
                digitalServiceVersionUid,
                heuristicContext.weightedAverages(),
                heuristicContext.locationWeights());

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
            Map<String, Double> weightedAverages,  // keyed by affectedAttribute name
            Map<String, Double> locationWeights
    ) {}

    /**
     * Builds the heuristic context by reading actual input values for the digital service version.
     *
     * Each numeric attribute is stored in weightedAverages under its affectedAttribute key.
     * To support a new attribute, add one entry here: choose the right repository, filter,
     * value extractor, and quantity extractor.
     */
    private HeuristicContext buildHeuristicContext(String digitalServiceVersionUid) {
        Map<String, Double> weightedAverages = new HashMap<>();

        // --- Shared collections (fetched once, reused across attribute blocks) ---

        List<InPhysicalEquipment> allPhysicalEquipments =
                inPhysicalEquipmentRepository.findByDigitalServiceVersionUid(digitalServiceVersionUid);

        List<InVirtualEquipment> allVirtualEquipments =
                inVirtualEquipmentRepository.findByDigitalServiceVersionUid(digitalServiceVersionUid);

        // --- Attribute blocks ---

        // "pue": quantity-weighted average PUE across private infrastructure servers.
        // Each server contributes its datacenter's PUE, weighted by the server quantity.
        Map<String, Double> pueByDatacenterName = inDatacenterRepository
                .findByDigitalServiceVersionUid(digitalServiceVersionUid)
                .stream()
                .filter(dc -> dc.getName() != null && dc.getPue() != null)
                .collect(Collectors.toMap(dc -> dc.getName(), dc -> dc.getPue(), (a, b) -> a));

        weightedAverages.put("pue",
                computeWeightedAverage(
                        allPhysicalEquipments.stream()
                                .filter(pe -> pe.getDatacenterName() != null
                                        && (pe.getType().equals(TYPE_SERVER) || pe.getType().equals(TYPE_DEDICATED_SERVER)))
                                .toList(),
                        pe -> pueByDatacenterName.get(pe.getDatacenterName()),
                        pe -> pe.getQuantity()
                ));

        // "workload": quantity-weighted average CPU workload across virtual equipments (cloud)
        weightedAverages.put("workload",
                computeWeightedAverage(
                        allVirtualEquipments,
                        ve -> ve.getWorkload(),
                        ve -> ve.getQuantity()
                ));

        // "yearlyUsageTimePerUser": simple average usage duration across terminal physical equipments.
        // Duration is per-user, quantity is irrelevant.
        weightedAverages.put("yearlyUsageTimePerUser",
                computeWeightedAverage(
                        allPhysicalEquipments.stream()
                                .filter(pe -> TYPE_TERMINAL.equals(pe.getType()))
                                .toList(),
                        pe -> pe.getDurationHour(),
                        pe -> 1.0   // unweighted: duration is independent of the number of terminals
                ));

        // "electricityConsumption": quantity-weighted average electricity consumption across private infrastructure servers.
        weightedAverages.put("electricityConsumption",
                computeWeightedAverage(
                        allPhysicalEquipments.stream()
                                .filter(pe -> pe.getType().equals(TYPE_SERVER) || pe.getType().equals(TYPE_DEDICATED_SERVER))
                                .toList(),
                        pe -> pe.getElectricityConsumption(),
                        pe -> pe.getQuantity()
                ));

        // Weighted locations from virtual equipments + datacenters
        Map<String, Double> locationWeights = computeLocationWeights(digitalServiceVersionUid, allVirtualEquipments, allPhysicalEquipments);

        return new HeuristicContext(weightedAverages, locationWeights);
    }

    /**
     * Computes the quantity-weighted average of a numeric attribute over a collection.
     *
     * Elements where valueExtractor returns null are excluded.
     * If quantityExtractor returns null, a quantity of 1.0 is assumed (simple average fallback).
     * Returns NaN if the collection is empty or all values are null.
     *
     * @param items           source collection
     * @param valueExtractor  extracts the numeric attribute value (e.g. workload, PUE)
     * @param quantityExtractor extracts the weight (e.g. number of instances)
     */
    private <T> Double computeWeightedAverage(
            List<T> items,
            java.util.function.Function<T, Double> valueExtractor,
            java.util.function.Function<T, Double> quantityExtractor) {

        double weightedSum = 0.0;
        double totalQuantity = 0.0;

        for (T item : items) {
            Double value = valueExtractor.apply(item);
            if (value == null) continue;
            double quantity = Optional.ofNullable(quantityExtractor.apply(item)).orElse(1.0);
            weightedSum    += value * quantity;
            totalQuantity  += quantity;
        }

        return totalQuantity > 0 ? weightedSum / totalQuantity : Double.NaN;
    }

    /**
         * Maps ISO-3 country codes to full country names used in the referential.
         * Only applied to public cloud locations.
         */
private String normalizeCloudLocation(String location) {
    if (location == null) return null;
    return switch (location.toUpperCase()) {
        case "ALB" -> "Albania";
        case "DZA" -> "Algeria";
        case "AND" -> "Andorra";
        case "AGO" -> "Angola";
        case "ARG" -> "Argentina";
        case "ARM" -> "Armenia";
        case "AUS" -> "Australia";
        case "AUT" -> "Austria";
        case "AZE" -> "Azerbaijan";
        case "BHS" -> "Bahamas";
        case "BHR" -> "Bahrain";
        case "BGD" -> "Bangladesh";
        case "BLR" -> "Belarus";
        case "BEL" -> "Belgium";
        case "BEN" -> "Benin";
        case "BOL" -> "Bolivia";
        case "BIH" -> "Bosnia-Herzegovina";
        case "BWA" -> "Botswana";
        case "BRA" -> "Brazil";
        case "BRN" -> "Brunei Darussalam";
        case "BGR" -> "Bulgaria";
        case "KHM" -> "Cambodia";
        case "CMR" -> "Cameroon";
        case "CAN" -> "Canada";
        case "CHL" -> "Chile";
        case "CHN" -> "China";
        case "COL" -> "Colombia";
        case "COG" -> "Congo";
        case "COD" -> "Congo, The Democratic Republic Of The";
        case "CRI" -> "Costa Rica";
        case "CIV" -> "Cote D'ivoire";
        case "HRV" -> "Croatia";
        case "CUB" -> "Cuba";
        case "CYP" -> "Cyprus";
        case "CZE" -> "Czech Republic";
        case "DNK" -> "Denmark";
        case "DOM" -> "Dominican Republic";
        case "ECU" -> "Ecuador";
        case "EGY" -> "Egypt";
        case "SLV" -> "El Salvador";
        case "ERI" -> "Eritrea";
        case "EST" -> "Estonia";
        case "ETH" -> "Ethiopia";
        case "FIN" -> "Finland";
        case "FRA" -> "France";
        case "GAB" -> "Gabon";
        case "GEO" -> "Georgia";
        case "DEU" -> "Germany";
        case "GHA" -> "Ghana";
        case "GRC" -> "Greece";
        case "GTM" -> "Guatemala";
        case "HTI" -> "Haiti";
        case "HND" -> "Honduras";
        case "HKG" -> "Hong Kong";
        case "HUN" -> "Hungary";
        case "ISL" -> "Iceland";
        case "IND" -> "India";
        case "IDN" -> "Indonesia";
        case "IRN" -> "Iran, Islamic Republic Of";
        case "IRQ" -> "Iraq";
        case "IRL" -> "Ireland";
        case "ISR" -> "Israel";
        case "ITA" -> "Italy";
        case "JAM" -> "Jamaica";
        case "JPN" -> "Japan";
        case "JOR" -> "Jordan";
        case "KAZ" -> "Kazakhstan";
        case "KEN" -> "Kenya";
        case "KWT" -> "Kuwait";
        case "KGZ" -> "Kyrgyzstan";
        case "LVA" -> "Latvia";
        case "LBN" -> "Lebanon";
        case "LBY" -> "Libya";
        case "LTU" -> "Lithuania";
        case "LUX" -> "Luxembourg";
        case "MYS" -> "Malaysia";
        case "MLT" -> "Malta";
        case "MEX" -> "Mexico";
        case "MDA" -> "Moldova, Republic Of";
        case "MNG" -> "Mongolia";
        case "MAR" -> "Morocco";
        case "MOZ" -> "Mozambique";
        case "MMR" -> "Myanmar";
        case "NAM" -> "Namibia";
        case "NPL" -> "Nepal";
        case "NLD" -> "Netherlands";
        case "NZL" -> "New Zealand";
        case "NIC" -> "Nicaragua";
        case "NGA" -> "Nigeria";
        case "PRK" -> "Korea, Democratic People's Repulic of";
        case "MKD" -> "North Macedonia";
        case "NOR" -> "Norway";
        case "OMN" -> "Oman";
        case "PAK" -> "Pakistan";
        case "PAN" -> "Panama";
        case "PRY" -> "Paraguay";
        case "PER" -> "Peru";
        case "PHL" -> "Philippines";
        case "POL" -> "Poland";
        case "PRT" -> "Portugal";
        case "QAT" -> "Qatar";
        case "ROU" -> "Romania";
        case "RUS" -> "Russian Federation";
        case "SAU" -> "Saudi Arabia";
        case "SEN" -> "Senegal";
        case "SRB" -> "Serbie";
        case "SGP" -> "Singapore";
        case "SVK" -> "Slovakia";
        case "SVN" -> "Slovenia";
        case "ZAF" -> "South Africa";
        case "KOR" -> "Korea, Republic Of";
        case "ESP" -> "Spain";
        case "LKA" -> "Sri Lanka";
        case "SDN" -> "Sudan";
        case "SUR" -> "Suriname";
        case "SWE" -> "Sweden";
        case "CHE" -> "Switzerland";
        case "SYR" -> "Syria";
        case "TWN" -> "Taiwan";
        case "TJK" -> "Tajikistan";
        case "TZA" -> "Tanzania, United Republic Of";
        case "THA" -> "Thailand";
        case "TGO" -> "Togo";
        case "TTO" -> "Trinidad and Tobago";
        case "TUN" -> "Tunisia";
        case "TUR" -> "Turkey";
        case "TKM" -> "Turkmenistan";
        case "UKR" -> "Ukraine";
        case "ARE" -> "United Arab Emirates";
        case "GBR" -> "United Kingdom";
        case "USA" -> "United States";
        case "URY" -> "Uruguay";
        case "UZB" -> "Uzbekistan";
        case "VEN" -> "Venezuela";
        case "VNM" -> "Vietnam";
        case "YEM" -> "Yemen";
        case "ZMB" -> "Zambia";
        case "ZWE" -> "Zimbabwe";
        case "EEE" -> "Europe";

        default -> location; 
    };
}


    /**
     * Collects all distinct non-null locations across virtual equipments and datacenters.
     */
    private Map<String, Double> computeLocationWeights(
            String digitalServiceVersionUid,
            List<InVirtualEquipment> allVirtualEquipments,
            List<InPhysicalEquipment> allPhysicalEquipments) {
        Map<String, Double> weights = new HashMap<>();
        // Cloud locations
        allVirtualEquipments.stream()
                .filter(ve -> ve.getLocation() != null && !ve.getLocation().isBlank()
                    && (INFRA_CLOUD_SERVICES.equals(ve.getInfrastructureType())
                                || INFRA_CLOUD.equals(ve.getInfrastructureType())))
                .forEach(ve -> {
                    // Normalization with ISO-3 country codes
                    String finalLocation = normalizeCloudLocation(ve.getLocation());

                    double qty = ve.getQuantity() != null ? ve.getQuantity() : 0.0;
                    weights.merge(finalLocation, qty, Double::sum);
                });
        
        // Private infra: only datacenters actually referenced by a server physical equipment
        allPhysicalEquipments.stream()
                .filter(pe -> pe.getType() != null
                        && (pe.getType().equals(TYPE_SERVER) || pe.getType().equals(TYPE_DEDICATED_SERVER))
                        && pe.getLocation() != null && !pe.getLocation().isBlank())
                .forEach(pe -> {
                    double qty = pe.getQuantity() != null ? pe.getQuantity() : 0.0;
                    weights.merge(pe.getLocation(), qty, Double::sum);
                });

        return weights;
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

        if ("location".equals(attribute)) {
            return computeLocationHeuristic(ctx.locationWeights());
        }

        if (recommendation.getHeuristicRange() == null) {
            return 0.5; // neutral -- no range defined for this attribute
        }

        Double actualValue = ctx.weightedAverages().get(attribute);
        if (actualValue == null) {
            log.warn("TOPSIS: unknown affectedAttribute '{}', using neutral score", attribute);
            return 0.5;
        }
        return computeNumericHeuristic(actualValue, recommendation.getHeuristicRange());
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
     * Computes heuristic score for location as the average quartile score
     * across all distinct locations in the digital service version.
     *
     * Quartile 1 (best mix) -> score 0.0 (not prioritized)
     * Quartile 4 (worst mix) -> score 1.0 (highly prioritized)
     * Unknown locations are ignored; if none found -> neutral 0.5
     */
    private double computeLocationHeuristic(Map<String, Double> locationWeights) {
        if (locationWeights == null || locationWeights.isEmpty()) return 0.5;

        try {
            Map<Pair<String, String>, Integer> quartiles = referentialService.getElectricityMixQuartiles();
            
            double totalWeightedScore = 0.0;
            double totalQuantity = 0.0;

            for (Map.Entry<String, Double> entry : locationWeights.entrySet()) {
                String location = entry.getKey();
                Double quantity = entry.getValue();
                
                Integer quartile = quartiles.get(Pair.of(location, CLIMATE_CHANGE));
                double score;
                
                if (quartile == null) {
                    score = 0.5; // Neutre si inconnu
                } else {
                    score = (quartile - 1.0) / 3.0; // 1->0.0, 4->1.0
                }
                
                totalWeightedScore += (score * quantity);
                totalQuantity += quantity;
            }

            if (totalQuantity == 0) return 0.5;

            double weightedAvg = totalWeightedScore / totalQuantity;
            log.info("TOPSIS: Weighted location heuristic score={} over {} total units", weightedAvg, totalQuantity);
            return weightedAvg;

        } catch (Exception e) {
            log.error("TOPSIS: failed to compute location heuristic", e);
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
        emissions.put(CATEGORY_TERMINAL,               0.0);
        emissions.put(CATEGORY_NETWORK,                0.0);
        emissions.put(CATEGORY_PRIVATE_INFRASTRUCTURE, 0.0);
        emissions.put(CATEGORY_PUBLIC_CLOUD,           0.0);

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
            case "Terminal"         -> CATEGORY_TERMINAL;
            case "Network"          -> CATEGORY_NETWORK;
            case "Server",
                 "Dedicated Server" -> CATEGORY_PRIVATE_INFRASTRUCTURE;
            default                 -> null;
        };
    }

    private String mapVirtualEquipmentTypeToCategory(String infrastructureType) {
        if (infrastructureType == null) return null;
        return switch (infrastructureType) {
            case "Cloud",
                 "CLOUD_SERVICES" -> CATEGORY_PUBLIC_CLOUD;
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