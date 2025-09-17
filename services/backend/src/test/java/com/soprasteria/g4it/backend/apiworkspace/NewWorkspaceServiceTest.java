/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.apiworkspace.business.NewWorkspaceService;
import com.soprasteria.g4it.backend.apiworkspace.model.OrganizationDetailsBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewWorkspaceServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private NewWorkspaceService newWorkspaceService;

    @Test
    void searchOrganizationsByDomainName_returnsOrganizationsWithWorkspaces_whenDomainMatches() {
        String userEmail = "user@example.com";
        String domainName = "example.com";

        Organization organization = Organization.builder().id(1L).name("Organization1").build();
        Workspace workspace = Workspace.builder().id(1L).name("Work1").status("Active").build();

        when(organizationRepository.findByAuthorizedDomainsContaining(domainName))
                .thenReturn(List.of(organization));
        when(workspaceRepository.findByOrganizationId(1L))
                .thenReturn(List.of(workspace));

        List<OrganizationDetailsBO> result = newWorkspaceService.searchOrganizationsByDomainName(userEmail);

        assertEquals(1, result.size());
        assertEquals("Organization1", result.getFirst().getName());
        assertEquals(1, result.getFirst().getWorkspaces().size());
        assertEquals("Work1", result.getFirst().getWorkspaces().getFirst().getName());
    }

    @Test
    void searchOrganizationsByDomainName_returnsEmptyList_whenNoOrganizationsMatchDomain() {
        String userEmail = "user@nonexistent.com";
        String domainName = "nonexistent.com";

        when(organizationRepository.findByAuthorizedDomainsContaining(domainName))
                .thenReturn(List.of());

        List<OrganizationDetailsBO> result = newWorkspaceService.searchOrganizationsByDomainName(userEmail);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchOrganizationsByDomainName_returnsOrganizationsWithEmptyWorkspaces_whenNoWorkspacesFound() {
        String userEmail = "user@example.com";
        String domainName = "example.com";

        Organization organization = Organization.builder().id(1L).name("Organization1").build();

        when(organizationRepository.findByAuthorizedDomainsContaining(domainName))
                .thenReturn(List.of(organization));
        when(workspaceRepository.findByOrganizationId(1L))
                .thenReturn(List.of());

        List<OrganizationDetailsBO> result = newWorkspaceService.searchOrganizationsByDomainName(userEmail);

        assertEquals(1, result.size());
        assertEquals("Organization1", result.getFirst().getName());
        assertTrue(result.getFirst().getWorkspaces().isEmpty());
    }

}
