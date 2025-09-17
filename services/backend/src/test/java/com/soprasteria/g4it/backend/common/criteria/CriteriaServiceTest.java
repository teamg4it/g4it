/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.criteria;

import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriteriaServiceTest {

    private static final String ORGANIZATION = "ORGANIZATION";
    private static final Long WORKSPACE_ID = 1L;
    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private WorkspaceService workspaceService;

    @InjectMocks
    private CriteriaService criteriaService;

    @Test
    void getSelectedCriteriaReturnsCriteriaByTypeWhenOrganizationExists() {
        List<String> mockCriteria = List.of("criteria1", "criteria2");
        Organization mockOrganization = new Organization();
        mockOrganization.setCriteria(mockCriteria);

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(mockOrganization));

        CriteriaByType result = criteriaService.getSelectedCriteria(ORGANIZATION);
        assertNotNull(result);
    }

    @Test
    void getSelectedCriteriaThrowsExceptionWhenOrganizationNotFound() {
        String organization = "nonExistentOrganization";

        when(organizationRepository.findByName(organization)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () -> {
            criteriaService.getSelectedCriteria(organization);
        });

        assertEquals("404", exception.getCode());
        assertEquals("Organization nonExistentOrganization not found", exception.getMessage());
    }

    @Test
    void getSelectedCriteriaForInventoryReturnsInventoryCriteriaWhenInventoryCriteriaProvided() {
        List<String> inventoryCriteria = List.of("inventory1", "inventory2");
        List<String> orgCriteria = List.of("criteria1", "criteria2");
        Workspace mockWorkspace = new Workspace();
        mockWorkspace.setCriteriaIs(List.of("workCriteria1"));

        Organization mockOrganization = new Organization();
        mockOrganization.setCriteria(orgCriteria);

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(mockOrganization));
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(mockWorkspace);

        CriteriaByType result = criteriaService.getSelectedCriteriaForInventory(ORGANIZATION, WORKSPACE_ID, inventoryCriteria);

        assertNotNull(result);
    }
    @Test
    void getSelectedCriteriaForInventoryReturnsWorkspaceCriteriaWhenNoInventoryCriteriaProvided() {
        List<String> workSpaceCriteria = List.of("orgCriteria1", "orgCriteria2");
        List<String> orgCriteria = List.of("criteria1", "criteria2");

        Organization mockOrg = new Organization();
        mockOrg.setCriteria(orgCriteria);

        Workspace mockWorkspace = new Workspace();
        mockWorkspace.setCriteriaIs(workSpaceCriteria);

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(mockOrg));
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(mockWorkspace);

        CriteriaByType result = criteriaService.getSelectedCriteriaForInventory(ORGANIZATION, WORKSPACE_ID, null);

        assertNotNull(result);
    }

    @Test
    void getSelectedCriteriaForInventoryReturnsOrganizationCriteriaWhenNoInventoryOrWorkspaceCriteriaProvided() {
        List<String> orgCriteria = List.of("criteria1", "criteria2");
        Workspace mockWorkspace = new Workspace();

        Organization mockOrg = new Organization();
        mockOrg.setCriteria(orgCriteria);

        when(mockWorkspace.getCriteriaIs()).thenReturn(null);
        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(mockOrg));
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(mockWorkspace);

        CriteriaByType result = criteriaService.getSelectedCriteriaForInventory(ORGANIZATION, WORKSPACE_ID, null);

        assertNotNull(result);
    }


    @Test
    void getSelectedCriteriaForDigitalServiceReturnsDigitalServiceCriteriaWhenDigitalServiceCriteriaProvided() {
        List<String> digitalServiceCriteria = List.of("digital1", "digital2");
        List<String> orgCriteria = List.of("criteria1", "criteria2");
        Workspace mockWorkspace = new Workspace();
        mockWorkspace.setCriteriaDs(List.of("workCriteriaDs1"));

        Organization mockOrg = new Organization();
        mockOrg.setCriteria(orgCriteria);

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(mockOrg));
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(mockWorkspace);

        CriteriaByType result = criteriaService.getSelectedCriteriaForDigitalService(ORGANIZATION, WORKSPACE_ID, digitalServiceCriteria);

        assertNotNull(result);
    }
    @Test
    void getSelectedCriteriaForDigitalServiceReturnsWorkspaceCriteriaWhenNoDigitalServiceCriteriaProvided() {
        List<String> workCriteria = List.of("workCriteria1", "workCriteria2");
        List<String> orgCriteria = List.of("criteria1", "criteria2");

        Workspace mockWork = new Workspace();
        mockWork.setCriteriaDs(workCriteria);

        Organization mockOrg = new Organization();
        mockOrg.setCriteria(orgCriteria);

        when(organizationRepository.findByName(ORGANIZATION)).thenReturn(Optional.of(mockOrg));
        when(workspaceService.getWorkspaceById(WORKSPACE_ID)).thenReturn(mockWork);

        CriteriaByType result = criteriaService.getSelectedCriteriaForDigitalService(ORGANIZATION, WORKSPACE_ID, null);

        assertNotNull(result);
    }


}