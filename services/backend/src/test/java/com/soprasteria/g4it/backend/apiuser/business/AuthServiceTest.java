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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.junit.jupiter.MockitoExtension;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

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
    @Mock
    private UserService userService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private DigitalServiceService digitalServiceService;
    @Mock
    private Environment environment;

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
        Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "controlAccess", user, "BadOrganization", WORKSPACE_ID));
    }

    @Test
    void testControlAccess_withUnknownWorkspace_thenUnauthorized() {
        Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "controlAccess", user, ORGANIZATION, 3L));
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
        Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "controlAccess", userWithoutRole, ORGANIZATION, WORKSPACE_ID));
    }

    static Stream<AdminUserTestCase> getAdminUserTestCases() {
        return Stream.of(
            new AdminUserTestCase(true, null, null, UserBO.builder().id(1).build(), null),
            new AdminUserTestCase(false, null, null, null, "The token is not a JWT token"),
            new AdminUserTestCase(false, Jwt.withTokenValue("token").header("alg", "none").claim("sub", "sub").build(), UserBO.builder().id(1).build(), null, "support.g4it@soprasteria.com")
        );
    }

    @ParameterizedTest
    @MethodSource("getAdminUserTestCases")
    void testGetAdminUser(AdminUserTestCase testCase) {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(testCase.noSecurityProfile);
        if (testCase.jwt != null) {
            Authentication auth = new JwtAuthenticationToken(testCase.jwt);
            SecurityContextHolder.getContext().setAuthentication(auth);
            Mockito.when(userService.getUserFromToken(testCase.jwt)).thenReturn(testCase.userFromToken);
            Mockito.when(userService.getUserByName(ArgumentMatchers.any())).thenReturn(null);
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        if (testCase.expected != null) {
            Mockito.when(userService.getNoSecurityUser(true)).thenReturn(testCase.expected);
            UserBO result = ReflectionTestUtils.invokeMethod(authService, "getAdminUser");
            Assertions.assertEquals(testCase.expected, result);
        } else {
            AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
                () -> ReflectionTestUtils.invokeMethod(authService, "getAdminUser"));
            Assertions.assertTrue(ex.getMessage().contains(testCase.expectedError));
        }
    }

    static class AdminUserTestCase {
        boolean noSecurityProfile;
        Jwt jwt;
        UserBO userFromToken;
        UserBO expected;
        String expectedError;
        AdminUserTestCase(boolean noSecurityProfile, Jwt jwt, UserBO userFromToken, UserBO expected, String expectedError) {
            this.noSecurityProfile = noSecurityProfile;
            this.jwt = jwt;
            this.userFromToken = userFromToken;
            this.expected = expected;
            this.expectedError = expectedError;
        }
    }

    @Test
    void testGetUser_noSecurityProfile() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(true);
        UserBO expected = UserBO.builder().id(1).build();
        Mockito.when(userService.getNoSecurityUser(true)).thenReturn(expected);
        UserBO result = ReflectionTestUtils.invokeMethod(authService, "getUser");
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testGetUser_invalidAuthentication() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(null);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "getUser"));
        Assertions.assertTrue(ex.getMessage().contains("The token is not a JWT token"));
    }

    @Test
    void testGetUser_userNotFound() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(false);
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "sub").build();
        Authentication auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
        Mockito.when(userService.getUserFromToken(jwt)).thenReturn(UserBO.builder().id(1).build());
        Mockito.when(userService.getUserByName(ArgumentMatchers.any())).thenReturn(null);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "getUser"));
        Assertions.assertTrue(ex.getMessage().contains("support.g4it@soprasteria.com"));
    }

    @Test
    void testGetOrganizationAndWorkspace_nominal() {
        String[] urlSplit = {"", "organizations", "org", "workspaces", "ws"};
        Pair<String, String> result = ReflectionTestUtils.invokeMethod(authService, "getOrganizationAndWorkspace", (Object) urlSplit);
        Assertions.assertEquals(Pair.of("org", "ws"), result);
    }

    @Test
    void testGetOrganizationAndWorkspace_nullInput() {
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "getOrganizationAndWorkspace", (Object) null));
        Assertions.assertTrue(ex.getMessage().contains("Unable to determine associated organization"));
    }

    @Test
    void testGetOrganizationAndWorkspace_invalidLength() {
        String[] urlSplit = {"", "organizations", "org"};
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "getOrganizationAndWorkspace", (Object) urlSplit));
        Assertions.assertTrue(ex.getMessage().contains("Unable to determine associated organization"));
    }

    @Test
    void testVerifyUserAuthentication_noSecurityProfile() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(true);
        UserBO expected = UserBO.builder().id(1).build();
        Mockito.when(userService.getNoSecurityUser(false)).thenReturn(expected);
        UserBO result = ReflectionTestUtils.invokeMethod(authService, "verifyUserAuthentication");
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testVerifyUserAuthentication_invalidAuthentication() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(null);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "verifyUserAuthentication"));
        Assertions.assertTrue(ex.getMessage().contains("User is not connected"));
    }

    @Test
    void testVerifyUserAuthentication_invalidPrincipal() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(false);
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(auth.getPrincipal()).thenReturn("notJwt");
        SecurityContextHolder.getContext().setAuthentication(auth);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "verifyUserAuthentication"));
        Assertions.assertTrue(ex.getMessage().contains("The token is not a JWT token"));
    }

    @Test
    void testVerifyUserAuthentication_nominal() {
        Mockito.when(environment.matchesProfiles(Constants.NOSECURITY)).thenReturn(false);
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "sub").build();
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserBO expected = UserBO.builder().id(1).build();
        Mockito.when(userService.getUserFromToken(jwt)).thenReturn(expected);
        UserBO result = ReflectionTestUtils.invokeMethod(authService, "verifyUserAuthentication");
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testGetJwtToken_userNotFound() {
        UserBO userInfo = UserBO.builder().email("test@test.com").build();
        Mockito.when(userService.getUserByName(userInfo)).thenReturn(null);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "getJwtToken", userInfo, "org", 1L));
        Assertions.assertTrue(ex.getMessage().contains("support.g4it@soprasteria.com"));
    }

    @Test
    void testCheckUserRightForDigitalService_digitalServiceExistsFalse() {
        String[] urlSplit = {"", "organizations", "org", "workspaces", "1", "digital-services", "dsUid"};
        Mockito.when(digitalServiceService.digitalServiceExists("org", 1L, "dsUid")).thenReturn(false);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "checkUserRightForDigitalService", (Object) urlSplit));
        Assertions.assertTrue(ex.getMessage().contains("does not exist or is not linked"));
    }

    @Test
    void testCheckUserRightForInventory_inventoryExistsFalse() {
        String[] urlSplit = {"", "organizations", "org", "organizations", "1", "inventories", "2"};
        Mockito.when(inventoryService.inventoryExists("org", 1L, 2L)).thenReturn(false);
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "checkUserRightForInventory", (Object) urlSplit));
        Assertions.assertTrue(ex.getMessage().contains("does not exist or is not linked"));
    }

    @Test
    void testCheckUserRightForInventory_invalidIdFormat() {
        String[] urlSplit = {"", "organizations", "org", "organizations", "notLong", "inventories", "notLong"};
        AuthorizationException ex = Assertions.assertThrows(AuthorizationException.class,
            () -> ReflectionTestUtils.invokeMethod(authService, "checkUserRightForInventory", (Object) urlSplit));
        Assertions.assertTrue(ex.getMessage().contains("must be in Long format"));
    }
}
