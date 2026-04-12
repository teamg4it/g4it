/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds.business;

import com.soprasteria.g4it.backend.apirecommendationds.mapper.RecommendationMapper;
import com.soprasteria.g4it.backend.apirecommendationds.modeldb.Recommendation;
import com.soprasteria.g4it.backend.server.gen.api.dto.InstantiatedRecommendationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
Service computing prioritized recommendations for a digital service version,
using the TOPSIS multi-criteria decision method.
 
  Current criteria (static phase):
    baseImpact  0=LOW, 1=MEDIUM, 2=HIGH; higher is better
    difficulty  1=EASY, 2=MEDIUM, 3=HARD;lower is better
  
 
  Future criteria (dynamic phase, not yet implemented):
 
    Proportion  : share of the component in total DS emissions
    heuristic   : attribute-level signal (e.g. high PUE detected)
 */
@Service
@AllArgsConstructor
@Slf4j
public class InstantiatedRecommendationService {

    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // Criterion weights (must sum to 1.0)
    // ─────────────────────────────────────────────────────────────────────────
    private static final double WEIGHT_IMPACT     = 0.6;
    private static final double WEIGHT_DIFFICULTY = 0.4;

    // ─────────────────────────────────────────────────────────────────────────
    // Difficulty mapping  (String → numeric score, lower = easier)
    // ─────────────────────────────────────────────────────────────────────────
    private static final int DIFFICULTY_EASY   = 1;
    private static final int DIFFICULTY_MEDIUM = 2;
    private static final int DIFFICULTY_HARD   = 3;

    /**
     * Returns recommendations for the given digital service version,
     * sorted by descending TOPSIS priority score.
     *
     * @param digitalServiceVersionUid the digital service version uid
     * @param organisationId           the organisation owning the digital service
     * @return list of instantiated recommendations sorted by priority (highest first)
     */
    public List<InstantiatedRecommendationRest> getInstantiatedRecommendations(
            final String digitalServiceVersionUid,
            final Long organisationId) {

        // 1. Fetch all recommendations visible to this organisation
        List<Recommendation> recommendations =
                getAllRecommendationsForOrganisation(organisationId);

        //Should not happen because of general recommendations
        if (recommendations.isEmpty()) {
            log.warn("TOPSIS: no recommendations found for organisationId={}", organisationId);
            return List.of();
        }

        // 2. Build numeric matrix  [n x 2]  — rows = alternatives, cols = criteria
        int n = recommendations.size();
        double[] impacts     = new double[n];
        double[] difficulties = new double[n];

        for (int i = 0; i < n; i++) {
            Recommendation r = recommendations.get(i);
            impacts[i]      = r.getBaseImpact()  != null ? r.getBaseImpact()  : 0;
            difficulties[i] = mapDifficulty(r.getDifficulty());
        }

        // 3. Run TOPSIS
        double[] priorities = topsis(impacts, difficulties, n);

        // 4. Build result DTOs
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
 

        // 5. Sort by priority descending
        result.sort(Comparator.comparingDouble(InstantiatedRecommendationRest::getPriority).reversed());

        log.info("TOPSIS: computed priorities for {} recommendations (dsVersionUid={})",
                n, digitalServiceVersionUid);

        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOPSIS implementation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes TOPSIS proximity scores for n alternatives with 2 criteria.
     *
     * <p>Criterion 1 — baseImpact  : beneficial  (higher raw value = better)
     * <p>Criterion 2 — difficulty  : cost        (lower  raw value = better)
     *
     * @param impacts      raw impact scores  [0, 1, 2]
     * @param difficulties raw difficulty scores [1, 2, 3]
     * @param n            number of alternatives
     * @return proximity scores in [0, 1], one per alternative
     */
    private double[] topsis(double[] impacts, double[] difficulties, int n) {

        // Step 1 — Euclidean normalisation per column
        double normImpact     = euclideanNorm(impacts,      n);
        double normDifficulty = euclideanNorm(difficulties, n);

        double[] rImpact     = new double[n];
        double[] rDifficulty = new double[n];

        for (int i = 0; i < n; i++) {
            rImpact[i]     = normImpact     > 0 ? impacts[i]      / normImpact     : 0;
            rDifficulty[i] = normDifficulty > 0 ? difficulties[i] / normDifficulty : 0;
        }

        // Step 2 — Apply weights
        double[] vImpact     = new double[n];
        double[] vDifficulty = new double[n];

        for (int i = 0; i < n; i++) {
            vImpact[i]     = rImpact[i]     * WEIGHT_IMPACT;
            vDifficulty[i] = rDifficulty[i] * WEIGHT_DIFFICULTY;
        }

        // Step 3 — Ideal best (A+) and worst (A-)
        //   impact     → beneficial → A+ = max, A- = min
        //   difficulty → cost       → A+ = min, A- = max
        double aPlusImpact      = max(vImpact,     n);
        double aMinusImpact     = min(vImpact,     n);
        double aPlusDifficulty  = min(vDifficulty, n);  // lower difficulty is better
        double aMinusDifficulty = max(vDifficulty, n);

        // Step 4 — Euclidean distances to A+ and A-
        double[] dPlus  = new double[n];
        double[] dMinus = new double[n];

        for (int i = 0; i < n; i++) {
            dPlus[i] = Math.sqrt(
                    sq(vImpact[i] - aPlusImpact) +
                    sq(vDifficulty[i] - aPlusDifficulty)
            );
            dMinus[i] = Math.sqrt(
                    sq(vImpact[i] - aMinusImpact) +
                    sq(vDifficulty[i] - aMinusDifficulty)
            );
        }

        // Step 5 — Proximity score  S* = d- / (d- + d+)
        double[] scores = new double[n];
        for (int i = 0; i < n; i++) {
            double denom = dMinus[i] + dPlus[i];
            scores[i] = denom > 0 ? dMinus[i] / denom : 0.5;
        }

        return scores;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

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

    /**
     * Maps the difficulty string stored in DB to a numeric cost score.
     * Null or unknown values default to MEDIUM.
     */
    private double mapDifficulty(String difficulty) {
        if (difficulty == null) return DIFFICULTY_MEDIUM;
        return switch (difficulty.toUpperCase()) {
            case "EASY"  -> DIFFICULTY_EASY;
            case "HARD"  -> DIFFICULTY_HARD;
            default      -> DIFFICULTY_MEDIUM;
        };
    }

    /**
     * Fetches all recommendations visible to this organisation
     * (general ones with null organisationId + org-specific ones).
     */
    private List<Recommendation> getAllRecommendationsForOrganisation(Long organisationId) {
        List<Recommendation> all = new ArrayList<>();
        all.addAll(recommendationService.findByOrganisationIdIsNull());
        all.addAll(recommendationService.findByOrganisationId(organisationId));
        return all;
    }
}