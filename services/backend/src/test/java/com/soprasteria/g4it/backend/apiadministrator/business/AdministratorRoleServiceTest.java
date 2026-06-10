/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.model.RoleBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministratorRoleServiceTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AdministratorRoleService administratorRoleService;

    private UserBO user;

    @BeforeEach
    void setUp() {
        user = UserBO.builder()
                .id(1L)
                .build();
    }

    @Test
    void shouldReturnAllRolesWhenUserHasAdminRightsOnOrganization() {
        List<RoleBO> roles = List.of(
                RoleBO.builder()
                        .id(1L)
                        .name("ADMIN")
                        .build()
        );

        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(true);
        when(roleService.getAllRolesBO()).thenReturn(roles);

        List<RoleBO> result = administratorRoleService.getAllRoles(user);

        assertEquals(roles, result);
        verify(roleService).getAllRolesBO();
    }

    @Test
    void shouldReturnAllRolesWhenUserHasAdminRightsOnWorkspace() {
        List<RoleBO> roles = List.of(
                RoleBO.builder()
                        .id(1L)
                        .name("ADMIN")
                        .build()
        );

        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(false);
        when(roleService.hasAdminRightsOnAnyWorkspace(user)).thenReturn(true);
        when(roleService.getAllRolesBO()).thenReturn(roles);

        List<RoleBO> result = administratorRoleService.getAllRoles(user);

        assertEquals(roles, result);
        verify(roleService).getAllRolesBO();
    }

    @Test
    void shouldThrowExceptionWhenGettingRolesWithoutAdminRights() {
        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(false);
        when(roleService.hasAdminRightsOnAnyWorkspace(user)).thenReturn(false);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> administratorRoleService.getAllRoles(user));

        assertTrue(exception.getMessage().contains("do not have admin role"));
    }

    @Test
    void shouldPassWhenUserHasAdminRightsOnAnyOrganization() {
        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(true);

        assertDoesNotThrow(
                () -> administratorRoleService.hasAdminRightsOnAnyOrganization(user));
    }

    @Test
    void shouldThrowWhenUserHasNoAdminRightsOnAnyOrganization() {
        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(false);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> administratorRoleService.hasAdminRightsOnAnyOrganization(user));

        assertTrue(exception.getMessage().contains("do not have admin role on any organization"));
    }

    @Test
    void shouldPassWhenUserHasAdminRightsOnAnyOrganizationOrWorkspace_Organization() {
        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(true);

        assertDoesNotThrow(
                () -> administratorRoleService.hasAdminRightsOnAnyOrganizationOrAnyWorkspace(user));
    }

    @Test
    void shouldPassWhenUserHasAdminRightsOnAnyOrganizationOrWorkspace_Workspace() {
        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(false);
        when(roleService.hasAdminRightsOnAnyWorkspace(user)).thenReturn(true);

        assertDoesNotThrow(
                () -> administratorRoleService.hasAdminRightsOnAnyOrganizationOrAnyWorkspace(user));
    }

    @Test
    void shouldThrowWhenUserHasNoAdminRightsOnAnyOrganizationOrWorkspace() {
        when(roleService.hasAdminRightsOnAnyOrganization(user)).thenReturn(false);
        when(roleService.hasAdminRightsOnAnyWorkspace(user)).thenReturn(false);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> administratorRoleService.hasAdminRightsOnAnyOrganizationOrAnyWorkspace(user));

        assertTrue(exception.getMessage().contains("do not have admin role"));
    }

    @Test
    void shouldPassWhenUserHasAdminRightsOnOrganization() {
        Long organizationId = 10L;
        Long workspaceId = 20L;

        when(roleService.hasAdminRightsOnOrganization(user, organizationId)).thenReturn(true);

        assertDoesNotThrow(
                () -> administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(
                        user, organizationId, workspaceId));
    }

    @Test
    void shouldPassWhenUserHasAdminRightsOnWorkspace() {
        Long organizationId = 10L;
        Long workspaceId = 20L;

        when(roleService.hasAdminRightsOnOrganization(user, organizationId)).thenReturn(false);
        when(roleService.hasAdminRightsOnWorkspace(user, workspaceId)).thenReturn(true);

        assertDoesNotThrow(
                () -> administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(
                        user, organizationId, workspaceId));
    }

    @Test
    void shouldThrowWhenUserHasNoAdminRightsOnOrganizationOrWorkspace() {
        Long organizationId = 10L;
        Long workspaceId = 20L;

        when(roleService.hasAdminRightsOnOrganization(user, organizationId)).thenReturn(false);
        when(roleService.hasAdminRightsOnWorkspace(user, workspaceId)).thenReturn(false);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(
                        user, organizationId, workspaceId));

        assertTrue(exception.getMessage().contains(
                "do not have admin role on organization"));
    }

    @Test
    void shouldPassWhenUserHasOrganizationAdminRights() {
        assertDoesNotThrow(
                () -> administratorRoleService.hasOrganizationAdminOrDomainAccess(
                        user, 10L, true, false));
    }

    @Test
    void shouldPassWhenUserHasDomainAuthorization() {
        assertDoesNotThrow(
                () -> administratorRoleService.hasOrganizationAdminOrDomainAccess(
                        user, 10L, false, true));
    }

    @Test
    void shouldThrowWhenUserHasNeitherOrganizationAdminRightsNorDomainAuthorization() {
        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> administratorRoleService.hasOrganizationAdminOrDomainAccess(
                        user, 10L, false, false));

        assertTrue(exception.getMessage().contains(
                "has no admin role on organization"));
    }
}
