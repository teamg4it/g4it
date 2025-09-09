/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserSearchBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriteriaRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministratorServiceTest {

    private final Workspace workspace = TestUtils.createWorkspace();
    @Mock
    CacheManager cacheManager;
    private long workspaceId;
    private long organizationId;
    // Given global
    @InjectMocks
    private AdministratorService administratorService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdministratorRoleService administratorRoleService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private OrganizationRestMapper organizationRestMapper;
    @Mock
    private UserService userService;

    @BeforeEach
    void init() {
        organizationId = workspace.getOrganization().getId();
        workspaceId = workspace.getId();
        Mockito.lenient().when(cacheManager.getCache(any())).thenReturn(Mockito.mock(Cache.class));
    }


    @Test
    void searchUserByName_withNoLinkedOrg() {
        String searchedUser = "stName";

        String authorizedDomains = "soprasteria.com,test.com";
        Organization organization = TestUtils.createOrganization(organizationId);
        organization.setAuthorizedDomains(authorizedDomains);
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), any(), any());

        when(organizationRepository.findById(any())).thenReturn(Optional.of(organization));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(
                List.of(User.builder().email("testName@soprasteria.com").firstName("test").lastName("Name").build()));

        List<UserSearchBO> searchedUsers = administratorService.searchUserByName(searchedUser, organizationId, workspace.getId(),
                TestUtils.createUserBOAdminSub());

        assertEquals(1, searchedUsers.size());
    }

    @Test
    void searchUserByName_withLinkedOrg() {
        String searchedUser = "test";
        String authorizedDomains = "soprasteria.com,test.com";
        Organization organization = TestUtils.createOrganization(organizationId);
        organization.setAuthorizedDomains(authorizedDomains);
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), any(), any());

        when(organizationRepository.findById(any())).thenReturn(Optional.of(organization));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(Collections.singletonList(User
                .builder().email("test@soprasteria.com")
                .userWorkspaces(List.of(UserWorkspace
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name(ROLE).build()))
                        .workspace(Workspace.builder().id(workspaceId).name(WORKSPACE)
                                .status(WorkspaceStatus.ACTIVE.name())
                                .organization(Organization.builder().id(2L).name(ORGANIZATION).build()).build())
                        .build()))
                .userOrganizations(List.of())
                .build()));
        List<UserSearchBO> searchedUsers;

        searchedUsers = administratorService.searchUserByName(searchedUser, organizationId, workspaceId, TestUtils.createUserBOAdminSub());

        assertEquals(1, searchedUsers.size());
    }

    @Test
    void updateOrganizationCriteria() {
        // Arrange
        organizationId = 1L;
        CriteriaRest criteriaRest = CriteriaRest.builder().criteria(List.of("New Criteria")).build();
        Organization organization = TestUtils.createOrganization(organizationId);
        organization.setCriteria(List.of("Old Criteria"));

        Organization updatedOrganization = TestUtils.createOrganization(organizationId);
        updatedOrganization.setCriteria(List.of("New Criteria"));
        OrganizationBO organizationBO = OrganizationBO.builder().id(organizationId)
                .name("SUBSCRIBER")
                .criteria(List.of("New Criteria")).build();
        doNothing().when(administratorRoleService).hasAdminRightsOnAnyOrganization(any());

        when(organizationService.getOrgById(organizationId)).thenReturn(organization);
        when(organizationRepository.save(any())).thenReturn(updatedOrganization);
        when(organizationRestMapper.toBusinessObject(updatedOrganization)).thenReturn(organizationBO);

        OrganizationBO result = administratorService.updateOrganizationCriteria(organizationId, criteriaRest, createUserBOAdminSub());

        assertThat(result.getCriteria()).isEqualTo(List.of("New Criteria"));

        verify(organizationRepository).save(updatedOrganization);
        verify(organizationRestMapper, times(1)).toBusinessObject(updatedOrganization);
        verify(userService).clearUserAllCache();
    }

}
