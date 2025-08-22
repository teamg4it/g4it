/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.criteria;

import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Common Criteria Service
 */
@Service
public class CriteriaService {

    @Autowired
    SubscriberRepository subscriberRepository;

    @Autowired
    private WorkspaceService workspaceService;

    private static final String ERROR_MESSAGE = "Subscriber %s not found";

    /**
     * Get the selected criteria from inventory, organization, subscriber
     * Empty list of nothing found
     *
     * @param subscriber the subscriber
     * @return the selected criteria list
     */
    public CriteriaByType getSelectedCriteria(String subscriber) {

        List<String> subscriberCriterias = subscriberRepository.findByName(subscriber)
                .orElseThrow(() -> new G4itRestException("404", String.format(ERROR_MESSAGE, subscriber))).getCriteria();

        return new CriteriaByType(subscriberCriterias, subscriberCriterias, null, null, null, null);
    }

    /**
     * Get the selected criteria from inventory, organization, subscriber
     * Empty list of nothing found
     *
     * @param subscriber         the subscriber
     * @param organizationId     the organization id
     * @param inventoryCriterias the inventory criterias
     * @return the criteria by type
     */
    public CriteriaByType getSelectedCriteriaForInventory(String subscriber, Long organizationId, List<String> inventoryCriterias) {

        List<String> subscriberCriterias = subscriberRepository.findByName(subscriber)
                .orElseThrow(() -> new G4itRestException("404", String.format(ERROR_MESSAGE, subscriber))).getCriteria();

        final Workspace workspace = workspaceService.getOrganizationById(organizationId);

        List<String> organizationCriteriaIs = workspace.getCriteriaIs();

        List<String> activeCriterias = null;
        if (inventoryCriterias != null) {
            activeCriterias = inventoryCriterias;
        } else if (organizationCriteriaIs != null) {
            activeCriterias = organizationCriteriaIs;
        } else if (subscriberCriterias != null) {
            activeCriterias = subscriberCriterias;
        }

        return new CriteriaByType(activeCriterias, subscriberCriterias, organizationCriteriaIs, null, inventoryCriterias, null);
    }

    /**
     * Get the selected criteria from inventory, organization, subscriber
     * Empty list of nothing found
     *
     * @param subscriber              the subscriber
     * @param organizationId          the organization id
     * @param digitalServiceCriterias the digital service id
     * @return the criteria by type
     */
    public CriteriaByType getSelectedCriteriaForDigitalService(String subscriber, Long organizationId, List<String> digitalServiceCriterias) {

        List<String> subscriberCriterias = subscriberRepository.findByName(subscriber)
                .orElseThrow(() -> new G4itRestException("404", String.format(ERROR_MESSAGE, subscriber))).getCriteria();

        final Workspace workspace = workspaceService.getOrganizationById(organizationId);

        List<String> organizationCriteriaDs = workspace.getCriteriaDs();

        List<String> activeCriterias = null;
        if (digitalServiceCriterias != null) {
            activeCriterias = digitalServiceCriterias;
        } else if (organizationCriteriaDs != null) {
            activeCriterias = organizationCriteriaDs;
        } else if (subscriberCriterias != null) {
            activeCriterias = subscriberCriterias;
        }

        return new CriteriaByType(activeCriterias, subscriberCriterias, null, organizationCriteriaDs, null, digitalServiceCriterias);
    }
}
