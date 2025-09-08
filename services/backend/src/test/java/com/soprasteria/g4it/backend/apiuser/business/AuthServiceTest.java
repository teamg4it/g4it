/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    public static final long WORKSPACE_ID = 1L;
    // Given global
    private static final String ORGANIZATION = "organization";
    private static final String WORKSPACE = "workpace";
    private static final List<String> roles = List.of("Role 1", "Role2");
    private static final UserBO user = UserBO.builder()
            .id(0)
            .organizations(
                    List.of(OrganizationBO.builder()
                            .name(ORGANIZATION)
                            .workspaces(List.of(WorkspaceBO.builder()
                                    .name(WORKSPACE)
                                    .id(WORKSPACE_ID)
                                    .roles(roles)
                                    .build()))
                            .roles(List.of())
                            .build())
            )
            .build();
    @InjectMocks
    private AuthService authService;

    @Test
    void testControlAccess_nominalCase_returnAllRoles() {
        List<String> actual = ReflectionTestUtils.invokeMethod(authService, "controlAccess", user, ORGANIZATION, WORKSPACE_ID);
        Assertions.assertEquals(roles, actual);
    }

    @Test
    void testControlAccess_OrganizationAdminCase_returnAllRoles() {
        var adminOrganization = UserBO.builder()
                .id(0)
                .organizations(
                        List.of(OrganizationBO.builder()
                                .name(ORGANIZATION)
                                .workspaces(List.of(WorkspaceBO.builder()
                                        .name(WORKSPACE)
                                        .roles(List.of())
                                        .build()))
                                .roles(List.of(Constants.ROLE_ORGANIZATION_ADMINISTRATOR))
                                .build())
                )
                .build();

        List<String> actual = ReflectionTestUtils.invokeMethod(authService, "controlAccess", adminOrganization, ORGANIZATION, WORKSPACE_ID);
        Assertions.assertEquals(Constants.ORGANIZATION_ROLES, actual);
    }


    @Test
    void testControlAccess_withUnknownOrganization_thenUnauthorized() {
        Assertions.assertThrows(AuthorizationException.class, () -> {
            ReflectionTestUtils.invokeMethod(authService, "controlAccess", user, "BadOrganization", WORKSPACE_ID);
        });
    }

    @Test
    void testControlAccess_withUnknownWorkspace_thenUnauthorized() {
        Assertions.assertThrows(AuthorizationException.class, () -> {
            ReflectionTestUtils.invokeMethod(authService, "controlAccess", user, ORGANIZATION, 3L);
        });
    }

    @Test
    void testControlAccess_withNoRole_thenForbidden() {
        final UserBO userWithoutRole = UserBO.builder()
                .id(0)
                .organizations(List.of(OrganizationBO.builder()
                        .name(ORGANIZATION)
                        .workspaces(List.of(WorkspaceBO.builder()
                                .name(WORKSPACE)
                                .id(WORKSPACE_ID)
                                .roles(List.of())
                                .build()))
                        .roles(List.of())
                        .build()))
                .build();

        Assertions.assertThrows(AuthorizationException.class, () -> {
            ReflectionTestUtils.invokeMethod(authService, "controlAccess", userWithoutRole, ORGANIZATION, WORKSPACE_ID);
        });
    }

}
