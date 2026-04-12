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
import com.soprasteria.g4it.backend.apirecommendationds.repository.RecommendationRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


/**
 * Service handling CRUD operations on eco-design recommendations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class RecommendationService {

    private RecommendationRepository recommendationRepository;
    private RecommendationMapper recommendationMapper;

    /**
     * Get all recommendations for an organisation.
     *
     * @param organisationId the organisation id
     * @return list of recommendations
     */
    public List<RecommendationDSRest> getRecommendationsByOrganisation(Long organisationId) {
        
        List<Recommendation> general_recommendation = recommendationRepository.findByOrganisationIdIsNull();
        List<Recommendation> organisation_recommendation = recommendationRepository.findByOrganisationId(organisationId);
        List<Recommendation> all = new ArrayList<>();
        all.addAll(general_recommendation);
        all.addAll(organisation_recommendation);
        List<RecommendationDSRest> result = recommendationMapper.toRestList(all);
        if (result.isEmpty()) {
        log.warn("LOG: No recommendations found for organisationId={}", organisationId);
        } else {
            log.info("LOG: {} recommendations found", result.size());
        }
        return result;
    }

    /**
     * Get all Recommendation entities for an organisation (if we need the whole object and not just the DTO)
     *
     * @param organisationId the organisation id
     * @return list of Recommendation entities
     */
    public List<Recommendation> findByOrganisationId(final Long organisationId) {
        return recommendationRepository.findByOrganisationId(organisationId);
    }

    /**
     * Get all general Recommendation entities (organisationId is null).
     *
     * @return list of general Recommendation entities
     */
    public List<Recommendation> findByOrganisationIdIsNull() {
        return recommendationRepository.findByOrganisationIdIsNull();
    }

    /**
     * Create a new recommendation.
     *
     * @param organisationId        the organisation id
     * @param recommendationDSRest the recommendation to create
     * @return the created recommendation
     */
    public RecommendationDSRest createRecommendation(final Long organisationId, final RecommendationDSRest recommendationDSRest) {
        Recommendation toCreate = recommendationMapper.toEntity(recommendationDSRest);
        toCreate.setOrganisationId(organisationId);
        return recommendationMapper.toRest(recommendationRepository.save(toCreate));
    }

    /**
     * Update an existing recommendation.
     *
     * @param organisationId        the organisation id
     * @param recommendationId      the recommendation id
     * @param recommendationDSRest the updated data
     * @return the updated recommendation
     */
    public RecommendationDSRest updateRecommendation(final Long organisationId, final Long recommendationId, final RecommendationDSRest recommendationDSRest) {
        Recommendation existing = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new G4itRestException("404",
                        String.format("Recommendation %d not found", recommendationId)));

        if (!organisationId.equals(existing.getOrganisationId())) {
            throw new G4itRestException("409",
                    String.format("Recommendation %d does not belong to organisation %d", recommendationId, organisationId));
        }

        Recommendation updates = recommendationMapper.toEntity(recommendationDSRest);
        recommendationMapper.merge(existing, updates);
        return recommendationMapper.toRest(recommendationRepository.save(existing));
    }

    /**
     * Delete a recommendation.
     *
     * @param organisationId   the organisation id
     * @param recommendationId the recommendation id
     */
    public void deleteRecommendation(final Long organisationId, final Long recommendationId) {
        Recommendation existing = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new G4itRestException("404",
                        String.format("Recommendation %d not found", recommendationId)));

        if (!organisationId.equals(existing.getOrganisationId())) {
            throw new G4itRestException("409",
                    String.format("Recommendation %d does not belong to organisation %d", recommendationId, organisationId));
        }

        recommendationRepository.deleteById(recommendationId);
    }
}
