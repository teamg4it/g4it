/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds.controller;

import com.soprasteria.g4it.backend.apirecommendationds.business.InstantiatedRecommendationService;
import com.soprasteria.g4it.backend.apirecommendationds.business.RecommendationService;
import com.soprasteria.g4it.backend.server.gen.api.RecommendationApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InstantiatedRecommendationRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Controller for eco-design recommendations.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class RecommendationController implements RecommendationApiDelegate {

    private InstantiatedRecommendationService instantiatedRecommendationService;
    private RecommendationService recommendationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InstantiatedRecommendationRest>> getInstantiatedRecommendations(
            final Long organisationId,
            final String digitalServiceVersionUid) {
        log.info("LOG: GET instantiated recommendations for organisationId={}, dsVersionUid={}",
                organisationId, digitalServiceVersionUid);
        return ResponseEntity.ok(
                instantiatedRecommendationService.getInstantiatedRecommendations(
                        digitalServiceVersionUid, organisationId)
        );
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RecommendationDSRest>> getRecommendationsByOrganisation(
            final Long organisationId) {

             log.info("LOG: GET /recommendations called with organisationId={}", organisationId);

            List<RecommendationDSRest> result =
                    recommendationService.getRecommendationsByOrganisation(organisationId);

            log.info("LOG: Returning {} recommendations", result.size());
            log.debug("LOG: Data: {}", result);

            return ResponseEntity.ok(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<RecommendationDSRest> createRecommendation(
            final Long organisationId,
            final RecommendationDSRest recommendationDSRest) {
        return new ResponseEntity<>(
                recommendationService.createRecommendation(organisationId, recommendationDSRest),
                HttpStatus.CREATED
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<RecommendationDSRest> updateRecommendation(
            final Long organisationId,
            final Long recommendationId,
            final RecommendationDSRest recommendationDSRest) {
        return ResponseEntity.ok(
                recommendationService.updateRecommendation(organisationId, recommendationId, recommendationDSRest)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteRecommendation(
            final Long organisationId,
            final Long recommendationId) {
        recommendationService.deleteRecommendation(organisationId, recommendationId);
        return ResponseEntity.noContent().build();
    }
}
