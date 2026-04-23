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
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final OrganizationRepository organizationRepository;

    /**
     * Get all recommendations for an organisation.
     *
     * @param organisationId the organisation id
     * @return list of recommendations
     */
   public List<RecommendationDSRest> getRecommendations(String organization, Long workspace) {

    Long organisationId = getOrganisationIdFromName(organization);

    List<Recommendation> general = recommendationRepository.findByOrganisationIdIsNull();
    List<Recommendation> specific = recommendationRepository.findByOrganisationId(organisationId);

    List<Recommendation> all = new ArrayList<>();
    all.addAll(general);
    all.addAll(specific);

      List<RecommendationDSRest> result = recommendationMapper.toRestList(all);
        if (result.isEmpty()) {
        log.warn("LOG: No recommendations found for organisationId={}", organisationId);
        } else {
            log.info("LOG: {} recommendations found", result.size());
        }
        return result;
}
private Long getOrganisationIdFromName(String name) {
    return(organizationRepository.findByName(name)
            .orElseThrow()
            .getId());
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
     * @param recommendationDSRest the recommendation to create
     * @return the created recommendation
     */
    public RecommendationDSRest createRecommendation(String organization, final RecommendationDSRest recommendationDSRest) {
        Long organisationId =  getOrganisationIdFromName(organization);
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
