/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.mapper.RoleMapper;
import com.soprasteria.g4it.backend.apiuser.model.RoleBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Test
    void hasAdminRightsOnAnyOrganization_hasAdminRole() {
        assertTrue(roleService.hasAdminRightsOnAnyOrganization(TestUtils.createUserBOAdminOrg()));
    }

    @Test
    void hasAdminRightsOnAnyOrganization_NoAdminRole() {
        assertFalse(roleService.hasAdminRightsOnAnyOrganization(TestUtils.createUserBONoRole()));
    }

    @Test
    void hasAdminRightsOnAnyOrganization_SuperAdminEmail() {
        assertTrue(roleService.hasAdminRightsOnAnyOrganization(TestUtils.createSuperAdminUserBO()));
    }

    @Test
    void hasAdminRightsOnAnyWorkspace_hasAdminRole() {
        UserBO user = TestUtils.createWorkspaceAdminUserBO(1L);
        assertTrue(roleService.hasAdminRightsOnAnyWorkspace(user));
    }

    @Test
    void hasAdminRightsOnAnyWorkspace_NoAdminRole() {
        assertFalse(roleService.hasAdminRightsOnAnyWorkspace(TestUtils.createUserBONoRole()));
    }

    @Test
    void hasAdminRightsOnAnyWorkspace_SuperAdminEmail() {
        assertTrue(roleService.hasAdminRightsOnAnyWorkspace(TestUtils.createSuperAdminUserBO()));
    }

    @Test
    void hasAdminRightsOnOrganization_hasAdminRole() {
        assertTrue(roleService.hasAdminRightsOnOrganization(TestUtils.createUserBOAdminOrg(), 2L));
    }

    @Test
    void hasAdminRightsOnOrganization_NoAdminRole() {
        // the user is organization admin on organization 2, not on 1
        assertFalse(roleService.hasAdminRightsOnOrganization(TestUtils.createUserBOAdminOrg(), 1L));
    }

    @Test
    void hasAdminRightsOnOrganization_SuperAdminEmail() {
        assertTrue(roleService.hasAdminRightsOnOrganization(TestUtils.createSuperAdminUserBO(), 42L));
    }

    @Test
    void hasAdminRightsOnWorkspace_hasAdminRole() {
        UserBO user = TestUtils.createWorkspaceAdminUserBO(1L);
        assertTrue(roleService.hasAdminRightsOnWorkspace(user, 1L));
    }

    @Test
    void hasAdminRightsOnWorkspace_NoAdminRole() {
        assertFalse(roleService.hasAdminRightsOnWorkspace(TestUtils.createUserBONoRole(), 1L));
    }

    @Test
    void hasAdminRightsOnWorkspace_SuperAdminEmail() {
        assertTrue(roleService.hasAdminRightsOnWorkspace(TestUtils.createSuperAdminUserBO(), 1L));
    }

    @Test
    void isUserDomainAuthorized_authorizedDomain() {
        // the user domain is present in organization's authorized domain
        assertTrue(roleService.isUserDomainAuthorized(TestUtils.createAuthorizedUserAdminOrg(), 1L));
    }
    @Test
    void isUserDomainAuthorized_noAuthorizedDomain() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(1L, "unitaire,dev", "unauthorized");
        assertFalse(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_nullAuthorizedDomain() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(1L, null, "unauthorized");
        assertFalse(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void getAllRolesBO_returnsMappedRoles() {
        List<Role> roles = List.of(Role.builder().name("role1").build());
        List<RoleBO> boList = List.of(RoleBO.builder().name("role1").build());
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleMapper.toDto(roles)).thenReturn(boList);
        assertEquals(boList, roleService.getAllRolesBO());
        verify(roleRepository).findAll();
        verify(roleMapper).toDto(roles);
    }

    @Test
    void getAllRoles_returnsRoles() {
        List<Role> roles = List.of(Role.builder().name("role1").build());
        when(roleRepository.findAll()).thenReturn(roles);
        assertEquals(roles, roleService.getAllRoles());
        verify(roleRepository).findAll();
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_organizationAdmin() {
        UserBO user = TestUtils.createUserBOAdminOrg();
        assertTrue(roleService.hasAdminRightOnOrganizationOrWorkspace(user, 2L, 99L));
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_workspaceAdmin() {
        UserBO user = TestUtils.createWorkspaceAdminUserBO(77L);
        assertTrue(roleService.hasAdminRightOnOrganizationOrWorkspace(user, 42L, 77L));
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_none() {
        UserBO user = TestUtils.createUserBONoRole();
        assertFalse(roleService.hasAdminRightOnOrganizationOrWorkspace(user, 1L, 1L));
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_superAdmin() {
        assertTrue(roleService.hasAdminRightOnOrganizationOrWorkspace(TestUtils.createSuperAdminUserBO(), 99L, 77L));
    }

    @Test
    void hasWorkspaceAdminRights_hasRole() {
        UserBO user = TestUtils.createWorkspaceAdminUserBO(10L);

        assertTrue(roleService.hasWorkspaceAdminRights(user, 10L));
    }

    @Test
    void hasWorkspaceAdminRights_noRole() {
        UserBO user = TestUtils.createUserBONoRole();

        assertFalse(roleService.hasWorkspaceAdminRights(user, 10L));
    }

    @Test
    void hasWorkspaceAdminRights_superAdmin() {
        assertTrue(roleService.hasWorkspaceAdminRights(
                TestUtils.createSuperAdminUserBO(), 10L
        ));
    }

    @Test
    void hasWorkspaceAdminRights_wrongWorkspaceId() {
        UserBO user = TestUtils.createWorkspaceAdminUserBO(10L);

        assertFalse(roleService.hasWorkspaceAdminRights(user, 99L));
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_bothFalse() {
        UserBO user = TestUtils.createUserBONoRole();

        assertFalse(roleService.hasAdminRightOnOrganizationOrWorkspace(user, 99L, 99L));
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_onlyWorkspaceAdmin() {
        UserBO user = TestUtils.createWorkspaceAdminUserBO(55L);

        assertTrue(roleService.hasAdminRightOnOrganizationOrWorkspace(user, 1L, 55L));
    }

    @Test
    void hasAdminRightOnOrganizationOrWorkspace_onlyOrgAdmin() {
        UserBO user = TestUtils.createUserBOAdminOrg();

        assertTrue(roleService.hasAdminRightOnOrganizationOrWorkspace(user, 2L, 999L));
    }

    @Test
    void isUserDomainAuthorized_multipleDomains_match() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                "abc.com,example.com,test.com",
                "example.com" // NOT full email
        );

        assertTrue(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_trimSpaces() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                " abc.com , example.com ",
                "example.com"
        );

        assertTrue(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_organizationNotFound() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                "example.com",
                "example.com"
        );

        assertFalse(roleService.isUserDomainAuthorized(user, 999L));
    }

    @Test
    void isUserDomainAuthorized_emptyAuthorizedDomains() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                "",
                "user@test.com"
        );

        assertFalse(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void hasAdminRightsOnAnyOrganization_noOrganizations() {
        UserBO user = UserBO.builder()
                .email("user@test.com")
                .organizations(List.of())
                .build();

        assertFalse(roleService.hasAdminRightsOnAnyOrganization(user));
    }


    @Test
    void hasAdminRightsOnWorkspace_emptyWorkspaces() {
        UserBO user = TestUtils.createUserBOAdminOrg();

        // FIX: use empty list instead of null
        user.getOrganizations().forEach(org -> org.setWorkspaces(List.of()));

        assertFalse(roleService.hasAdminRightsOnWorkspace(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_noMatch() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                "abc.com,test.com",
                "example.com"
        );

        assertFalse(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_emptyDomains() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                "",
                "example.com"
        );

        assertFalse(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_nullDomains() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                null,
                "example.com"
        );

        assertFalse(roleService.isUserDomainAuthorized(user, 1L));
    }

    @Test
    void isUserDomainAuthorized_exactExtractionCheck() {
        UserBO user = TestUtils.createUserBOWithAuthorizedDomains(
                1L,
                "unitaire",
                "unitaire"
        );

        // email = test@unitaire → domain = "unitaire"
        assertTrue(roleService.isUserDomainAuthorized(user, 1L));
    }

}
