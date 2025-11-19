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
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
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
    OrganizationRepository organizationRepository;

    @Autowired
    private WorkspaceService workspaceService;

    private static final String ERROR_MESSAGE = "Organization %s not found";

    /**
     * Get the selected criteria from inventory, workspace, organization
     * Empty list of nothing found
     *
     * @param organization the organization
     * @return the selected criteria list
     */
    public CriteriaByType getSelectedCriteria(String organization) {

        List<String> organizationCriteria = organizationRepository.findByName(organization)
                .orElseThrow(() -> new G4itRestException("404", String.format(ERROR_MESSAGE, organization))).getCriteria();

        return new CriteriaByType(organizationCriteria, organizationCriteria, null, null, null, null);
    }

    /**
     * Get the selected criteria from inventory, workspace, organization
     * Empty list of nothing found
     *
     * @param organization         the organization
     * @param workspaceId     the workspace id
     * @param inventoryCriteria the inventory criteria
     * @return the criteria by type
     */
    public CriteriaByType getSelectedCriteriaForInventory(String organization, Long workspaceId, List<String> inventoryCriteria) {

        List<String> organizationCriteria = organizationRepository.findByName(organization)
                .orElseThrow(() -> new G4itRestException("404", String.format(ERROR_MESSAGE, organization))).getCriteria();

        final Workspace workspace = workspaceService.getWorkspaceById(workspaceId);

        List<String> workspaceCriteriaIs = workspace.getCriteriaIs();

        List<String> activeCriteria = null;
        if (inventoryCriteria != null) {
            activeCriteria = inventoryCriteria;
        } else if (workspaceCriteriaIs != null) {
            activeCriteria = workspaceCriteriaIs;
        } else if (organizationCriteria != null) {
            activeCriteria= organizationCriteria;
        }

        return new CriteriaByType(activeCriteria, organizationCriteria, workspaceCriteriaIs, null, inventoryCriteria, null);
    }

    /**
     * Get the selected criteria from inventory, workspace, organization
     * Empty list of nothing found
     *
     * @param organization              the organization
     * @param workspaceId          the workspace id
     * @param digitalServiceVersionCriteria the digital service criteria
     * @return the criteria by type
     */
    public CriteriaByType getSelectedCriteriaForDigitalService(String organization, Long workspaceId, List<String> digitalServiceVersionCriteria) {

        List<String> organizationCriteria = organizationRepository.findByName(organization)
                .orElseThrow(() -> new G4itRestException("404", String.format(ERROR_MESSAGE, organization))).getCriteria();

        final Workspace workspace = workspaceService.getWorkspaceById(workspaceId);

        List<String> workspaceCriteriaDs = workspace.getCriteriaDs();

        List<String> activeCriteria = null;
        if (digitalServiceVersionCriteria != null) {
            activeCriteria = digitalServiceVersionCriteria;
        } else if (workspaceCriteriaDs != null) {
            activeCriteria = workspaceCriteriaDs;
        } else if (organizationCriteria != null) {
            activeCriteria = organizationCriteria;
        }

        return new CriteriaByType(activeCriteria, organizationCriteria, null, workspaceCriteriaDs, null, digitalServiceVersionCriteria);
    }
}
