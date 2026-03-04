/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendation.business;

import com.soprasteria.g4it.backend.apirecommendation.mapper.RecommendationMapper;
import com.soprasteria.g4it.backend.apirecommendation.modeldb.Recommendation;
import com.soprasteria.g4it.backend.apirecommendation.repository.RecommendationRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.EcoRecommendationRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public List<EcoRecommendationRest> getRecommendationsByOrganisation(final Long organisationId) {
        return recommendationMapper.toRestList(recommendationRepository.findByOrganisationId(organisationId));
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
     * Create a new recommendation.
     *
     * @param organisationId        the organisation id
     * @param ecoRecommendationRest the recommendation to create
     * @return the created recommendation
     */
    public EcoRecommendationRest createRecommendation(final Long organisationId, final EcoRecommendationRest ecoRecommendationRest) {
        Recommendation toCreate = recommendationMapper.toEntity(ecoRecommendationRest);
        toCreate.setOrganisationId(organisationId);
        return recommendationMapper.toRest(recommendationRepository.save(toCreate));
    }

    /**
     * Update an existing recommendation.
     *
     * @param organisationId        the organisation id
     * @param recommendationId      the recommendation id
     * @param ecoRecommendationRest the updated data
     * @return the updated recommendation
     */
    public EcoRecommendationRest updateRecommendation(final Long organisationId, final Long recommendationId, final EcoRecommendationRest ecoRecommendationRest) {
        Recommendation existing = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new G4itRestException("404",
                        String.format("Recommendation %d not found", recommendationId)));

        if (!organisationId.equals(existing.getOrganisationId())) {
            throw new G4itRestException("409",
                    String.format("Recommendation %d does not belong to organisation %d", recommendationId, organisationId));
        }

        Recommendation updates = recommendationMapper.toEntity(ecoRecommendationRest);
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
