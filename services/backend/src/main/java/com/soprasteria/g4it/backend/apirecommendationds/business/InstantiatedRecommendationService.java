/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds.business;

import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.business.OutPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutVirtualEquipmentService;
import com.soprasteria.g4it.backend.apirecommendationds.mapper.RecommendationMapper;
import com.soprasteria.g4it.backend.apirecommendationds.modeldb.Recommendation;
import com.soprasteria.g4it.backend.server.gen.api.dto.InstantiatedRecommendationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service computing prioritized recommendations for a digital service version,
 * using the TOPSIS multi-criteria decision method.
 *
 * Criteria:
 *   proportion  (weight=0.5) -- share of category in total CLIMATE_CHANGE emissions -- beneficial
 *   baseImpact  (weight=0.3) -- 0=LOW, 1=MEDIUM, 2=HIGH -- beneficial
 *   difficulty  (weight=0.2) -- 1=EASY, 2=MEDIUM, 3=HARD -- cost (lower is better)
 *
 * Future criteria (not yet implemented):
 *   heuristic -- attribute-level signal (e.g. high PUE detected)
 */
@Service
@AllArgsConstructor
@Slf4j
public class InstantiatedRecommendationService {

    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;
    private final OutPhysicalEquipmentService outPhysicalEquipmentService;
    private final OutVirtualEquipmentService outVirtualEquipmentService;
    private final DigitalServiceVersionRepository digitalServiceVersionRepository;

    // Criterion weights (must sum to 1.0)
    private static final double WEIGHT_PROPORTION = 0.5;
    private static final double WEIGHT_IMPACT     = 0.3;
    private static final double WEIGHT_DIFFICULTY = 0.2;

    // Difficulty mapping (String -> numeric score, lower = easier)
    private static final int DIFFICULTY_EASY   = 1;
    private static final int DIFFICULTY_MEDIUM = 2;
    private static final int DIFFICULTY_HARD   = 3;

    // Criterion value for emission filtering
    private static final String CLIMATE_CHANGE = "CLIMATE_CHANGE";

    /**
     * Returns recommendations for the given digital service version,
     * sorted by descending TOPSIS priority score.
     */
    public List<InstantiatedRecommendationRest> getInstantiatedRecommendations(
            final String digitalServiceVersionUid,
            final Long organisationId) {

        // 1. Fetch all recommendations visible to this organisation
        List<Recommendation> recommendations = getAllRecommendationsForOrganisation(organisationId);

        if (recommendations.isEmpty()) {
            log.warn("TOPSIS: no recommendations found for organisationId={}", organisationId);
            return List.of();
        }

        // 2. Compute emission proportions per category from last evaluation
        Map<String, Double> categoryProportions = computeCategoryProportions(digitalServiceVersionUid);
        log.info("TOPSIS: category proportions for dsVersionUid={}: {}", digitalServiceVersionUid, categoryProportions);

        // 3. Build numeric matrix [n x 3]
        int n = recommendations.size();
        double[] impacts      = new double[n];
        double[] difficulties = new double[n];
        double[] proportions  = new double[n];

        for (int i = 0; i < n; i++) {
            Recommendation r = recommendations.get(i);
            impacts[i]      = r.getBaseImpact() != null ? r.getBaseImpact() : 0;
            difficulties[i] = mapDifficulty(r.getDifficulty());
            proportions[i]  = computeMaxProportion(r.getCategory(), categoryProportions);
        }

        // 4. Run TOPSIS
        double[] priorities = topsis(proportions, impacts, difficulties, n);

        // 5. Build result DTOs
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

        // 6. Sort by priority descending
        result.sort(Comparator.comparingDouble(InstantiatedRecommendationRest::getPriority).reversed());

        log.info("TOPSIS: computed priorities for {} recommendations (dsVersionUid={})", n, digitalServiceVersionUid);

        return result;
    }

    // -------------------------------------------------------------------------
    // Emission proportion computation
    // -------------------------------------------------------------------------

    /**
     * Computes the share of total CLIMATE_CHANGE emissions for each recommendation category.
     *
     * Category mapping:
     *   TERMINAL               <- OutPhysicalEquipment where equipmentType = "Terminal"
     *   NETWORK                <- OutPhysicalEquipment where equipmentType = "Network"
     *   PRIVATE_INFRASTRUCTURE <- OutPhysicalEquipment where equipmentType = "Server" or "Dedicated Server"
     *   PUBLIC_CLOUD           <- OutVirtualEquipment   where infrastructureType = "Cloud"
     */
    private Map<String, Double> computeCategoryProportions(String digitalServiceVersionUid) {
        Map<String, Double> emissions = new HashMap<>();
        emissions.put("TERMINAL",               0.0);
        emissions.put("NETWORK",                0.0);
        emissions.put("PRIVATE_INFRASTRUCTURE", 0.0);
        emissions.put("PUBLIC_CLOUD",           0.0);

        // Physical equipments
        List<OutPhysicalEquipmentRest> physicalEquipments =
                outPhysicalEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid);

        log.info("TOPSIS: physical equipments for dsVersionUid={}: count={}, sample equipmentTypes={}",
                digitalServiceVersionUid,
                physicalEquipments.size(),
                physicalEquipments.stream().map(OutPhysicalEquipmentRest::getEquipmentType).distinct().toList());

        for (OutPhysicalEquipmentRest eq : physicalEquipments) {
            if (!CLIMATE_CHANGE.equals(eq.getCriterion())) continue;
            if (eq.getUnitImpact() == null) continue;
            String category = mapPhysicalEquipmentTypeToCategory(eq.getEquipmentType());
            if (category != null) {
                emissions.merge(category, eq.getUnitImpact(), Double::sum);
            }
        }

        // Virtual equipments
        List<OutVirtualEquipmentRest> virtualEquipments =
                outVirtualEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid);

        log.info("TOPSIS: virtual equipments for dsVersionUid={}: count={}, sample infrastructureTypes={}",
                digitalServiceVersionUid,
                virtualEquipments.size(),
                virtualEquipments.stream().map(OutVirtualEquipmentRest::getInfrastructureType).distinct().toList());

        for (OutVirtualEquipmentRest eq : virtualEquipments) {
            if (!CLIMATE_CHANGE.equals(eq.getCriterion())) continue;
            if (eq.getUnitImpact() == null) continue;
            String category = mapVirtualEquipmentTypeToCategory(eq.getInfrastructureType());
            if (category != null) {
                emissions.merge(category, eq.getUnitImpact(), Double::sum);
            }
        }

        // Convert to proportions
        double total = emissions.values().stream().mapToDouble(Double::doubleValue).sum();

        if (total == 0) {
            log.warn("TOPSIS: no CLIMATE_CHANGE emissions found for dsVersionUid={}, proportions default to 0", digitalServiceVersionUid);
            return emissions;
        }

        Map<String, Double> proportions = new HashMap<>();
        emissions.forEach((category, impact) -> proportions.put(category, impact / total));
        return proportions;
    }

    /**
     * For a multi-category recommendation, returns the max proportion among its categories.
     */
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
            case "Cloud" -> "PUBLIC_CLOUD";
            default      -> null;
        };
    }

    // -------------------------------------------------------------------------
    // TOPSIS implementation
    // -------------------------------------------------------------------------

    /**
     * Computes TOPSIS proximity scores for n alternatives with 3 criteria.
     *
     * Criterion 1 -- proportion : beneficial (higher = more impactful category)
     * Criterion 2 -- baseImpact : beneficial (higher raw value = better)
     * Criterion 3 -- difficulty : cost       (lower  raw value = better)
     */
    private double[] topsis(double[] proportions, double[] impacts, double[] difficulties, int n) {

        // Step 1 -- Euclidean normalisation per column
        double normProportion = euclideanNorm(proportions, n);
        double normImpact     = euclideanNorm(impacts,     n);
        double normDifficulty = euclideanNorm(difficulties, n);

        double[] rProportion = new double[n];
        double[] rImpact     = new double[n];
        double[] rDifficulty = new double[n];

        for (int i = 0; i < n; i++) {
            rProportion[i] = normProportion > 0 ? proportions[i]  / normProportion : 0;
            rImpact[i]     = normImpact     > 0 ? impacts[i]      / normImpact     : 0;
            rDifficulty[i] = normDifficulty > 0 ? difficulties[i] / normDifficulty : 0;
        }

        // Step 2 -- Apply weights
        double[] vProportion = new double[n];
        double[] vImpact     = new double[n];
        double[] vDifficulty = new double[n];

        for (int i = 0; i < n; i++) {
            vProportion[i] = rProportion[i] * WEIGHT_PROPORTION;
            vImpact[i]     = rImpact[i]     * WEIGHT_IMPACT;
            vDifficulty[i] = rDifficulty[i] * WEIGHT_DIFFICULTY;
        }

        // Step 3 -- Ideal best (A+) and worst (A-)
        //   proportion -> beneficial -> A+ = max, A- = min
        //   impact     -> beneficial -> A+ = max, A- = min
        //   difficulty -> cost       -> A+ = min, A- = max
        double aPlusProportion  = max(vProportion, n);
        double aMinusProportion = min(vProportion, n);
        double aPlusImpact      = max(vImpact,     n);
        double aMinusImpact     = min(vImpact,     n);
        double aPlusDifficulty  = min(vDifficulty, n);
        double aMinusDifficulty = max(vDifficulty, n);

        // Step 4 -- Euclidean distances to A+ and A-
        double[] dPlus  = new double[n];
        double[] dMinus = new double[n];

        for (int i = 0; i < n; i++) {
            dPlus[i] = Math.sqrt(
                    sq(vProportion[i] - aPlusProportion)  +
                    sq(vImpact[i]     - aPlusImpact)      +
                    sq(vDifficulty[i] - aPlusDifficulty)
            );
            dMinus[i] = Math.sqrt(
                    sq(vProportion[i] - aMinusProportion) +
                    sq(vImpact[i]     - aMinusImpact)     +
                    sq(vDifficulty[i] - aMinusDifficulty)
            );
        }

        // Step 5 -- Proximity score  S* = d- / (d- + d+)
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