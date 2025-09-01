/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpsertRest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.ROLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministratorWorkspaceServiceTest {

    @InjectMocks
    private AdministratorWorkspaceService administratorWorkspaceService;

    private long organizationId;

    @Mock
    UserRepository userRepository;

    @Mock
    UserWorkspaceRepository userWorkspaceRepository;

    @Mock
    WorkspaceRepository workspaceRepository;

    @Mock
    UserRoleWorkspaceRepository userRoleWorkspaceRepository;

    @Mock
    private AdministratorRoleService administratorRoleService;
    @Mock
    private RoleService roleService;
    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private AuthService authService;

    @Mock
    UserOrganizationRepository userOrganizationRepository;

    @Mock
    UserService userService;

    @Test
    void linkUserToWorkspace_WithRoles() {

        long userId = 1L;

        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));


        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId,
                Collections.singletonList(userRoleRest));

        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(organizationId, userId)).thenReturn(Optional.empty());
        when(workspaceRepository.findById(organizationId)).thenReturn(Optional.of(TestUtils.createOrganization()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorWorkspaceService.linkUserToWorkspace(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(ROLE, users.getFirst().getRoles().getFirst());
        verify(userWorkspaceRepository, times(1)).save(any(UserWorkspace.class));
        verify(userRoleWorkspaceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void linkUserToWorkspace_WithoutRoles() {

        long userId = 1L;
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of());
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId, Collections.singletonList(userRoleRest));

        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(organizationId, userRoleRest.getUserId())).thenReturn(Optional.empty());
        when(workspaceRepository.findById(organizationId)).thenReturn(Optional.of(TestUtils.createOrganization()));
        when(userRepository.findById(userRoleRest.getUserId())).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorWorkspaceService.linkUserToWorkspace(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(List.of(), users.getFirst().getRoles());
        verify(userWorkspaceRepository, times(1)).save(any(UserWorkspace.class));
    }

    @Test
    void linkUserToWorkspace_NotEmpty() {

        long userId = 1L;

        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId,
                Collections.singletonList(userRoleRest));
        UserWorkspace userWorkspace = TestUtils.createUserOrganization(organizationId, userId);

        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(organizationId, userRoleRest.getUserId())).thenReturn(Optional.ofNullable(userWorkspace));
        when(workspaceRepository.findById(organizationId)).thenReturn(Optional.of(TestUtils.createOrganization()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorWorkspaceService.linkUserToWorkspace(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(ROLE, users.getFirst().getRoles().getFirst());
        verify(userRoleWorkspaceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getWorkspacesWithSubscriberAndOrganizationNull() {
        Long subscriberId = null;
        Long orgId = null;
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));

        doNothing().when(administratorRoleService).hasAdminRightsOnAnyOrganizationOrAnyWorkspace(userBO);

        List<OrganizationBO> result = administratorWorkspaceService.getWorkspaces(subscriberId, orgId, userBO);

        verify(administratorRoleService).hasAdminRightsOnAnyOrganizationOrAnyWorkspace(userBO);
        assertEquals(1, result.size());

        OrganizationBO organizationBO = result.getFirst();
        assertEquals(1, organizationBO.getOrganizations().size());
    }

    @Test
    void getWorkspacesWithoutAdminRights() {
        UserBO userBO = UserBO.builder().id(1L).build();

        doThrow(new AuthorizationException(403, "User with id '1' do not have admin role")).when(administratorRoleService).hasAdminRightsOnAnyOrganizationOrAnyWorkspace(userBO);

        AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
            administratorWorkspaceService.getWorkspaces(null, null, userBO);
        });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("do not have admin role"));
    }

    @Test
    void updateWorkspace() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        Long orgId = 33L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        WorkspaceBO updatedOrganization = WorkspaceBO.builder().id(orgId).name("UpdatedName").build();

        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(userBO, subscriberId, orgId);
        when(workspaceService.updateWorkspace(orgId, workspaceUpsertRest, userBO.getId())).thenReturn(updatedOrganization);

        WorkspaceBO result = administratorWorkspaceService.updateWorkspace(orgId, workspaceUpsertRest, userBO);

        verify(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(userBO, subscriberId, orgId);
        verify(workspaceService).updateWorkspace(orgId, workspaceUpsertRest, userBO.getId());
        verify(userService).clearUserAllCache();

        assertEquals(orgId, result.getId());
        assertEquals("UpdatedName", result.getName());
    }

    @Test
    void updateWorkspaceNoAdminRights() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        Long orgId = 33L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "User with id '1' do not have admin role on subscriber '1' or organization '1'")).when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(userBO, subscriberId, orgId);

        assertThrows(AuthorizationException.class, () -> {
            administratorWorkspaceService.updateWorkspace(orgId, workspaceUpsertRest, userBO);
        });

        verify(workspaceService, never()).updateWorkspace(any(), any(), any());
        verify(userService, never()).clearUserAllCache();
    }

    @Test
    void createWorkspaceWithAdminRole() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        WorkspaceBO expectedOrg = WorkspaceBO.builder().id(33L).build();

        when(workspaceService.createWorkspace(workspaceUpsertRest, userBO, subscriberId)).thenReturn(expectedOrg);
        when(roleService.isUserDomainAuthorized(userBO, subscriberId)).thenReturn(true);
        when(roleService.hasAdminRightsOnOrganization(userBO, subscriberId)).thenReturn(true);
        WorkspaceBO result = administratorWorkspaceService.createWorkspace(workspaceUpsertRest, userBO, true);

        verify(administratorRoleService).hasOrganizationAdminOrDomainAccess(userBO, subscriberId, true, true);
        verify(workspaceService).createWorkspace(workspaceUpsertRest, userBO, subscriberId);
        verify(userService).clearUserCache(userBO);

        assertEquals(expectedOrg, result);
    }

    @Test
    void createWorkspaceWithoutAdminRole() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        WorkspaceBO expectedOrg = WorkspaceBO.builder().id(1L).name(organizationName).build();

        Workspace org = TestUtils.createOrganization();
        User userEntity = User.builder().id(1L).build();

        when(workspaceService.createWorkspace(workspaceUpsertRest, userBO, subscriberId)).thenReturn(expectedOrg);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(org));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(org.getId(), userBO.getId())).thenReturn(Optional.empty());
        when(roleService.isUserDomainAuthorized(userBO, subscriberId)).thenReturn(true);
        when(roleService.hasAdminRightsOnOrganization(userBO, subscriberId)).thenReturn(false);

        WorkspaceBO result = administratorWorkspaceService.createWorkspace(workspaceUpsertRest, userBO, false);

        verify(workspaceService).createWorkspace(workspaceUpsertRest, userBO, subscriberId);
        verify(roleService).hasAdminRightsOnOrganization(userBO, subscriberId);
        verify(userService).clearUserCache(userBO);
        verify(userRepository).findById(1L);
        verify(userWorkspaceRepository).save(any());
        verify(userRoleWorkspaceRepository).saveAll(any());

        assertEquals(expectedOrg, result);
    }

    @Test
    void createWorkspace_NoAdminRightsAnd_NoDomainAuthorization_ThrowException() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;

        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);

        when(roleService.hasAdminRightsOnOrganization(userBO, subscriberId)).thenReturn(false);
        when(roleService.isUserDomainAuthorized(userBO, subscriberId)).thenReturn(false);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN,
                "User with id 1 has no admin role on subscriber 1 or has domain not authorized."))
                .when(administratorRoleService).hasOrganizationAdminOrDomainAccess(userBO,
                        subscriberId, false, false);

        assertThrows(AuthorizationException.class, () -> {
            administratorWorkspaceService.createWorkspace(workspaceUpsertRest, userBO, true);
        });

        verify(roleService).hasAdminRightsOnOrganization(userBO, subscriberId);
        verify(roleService).isUserDomainAuthorized(userBO, subscriberId);
    }

    @Test
    void getUsersOfWorkspaceNotFound() {
        Long orgId = 12L;
        when(workspaceRepository.findById(orgId)).thenReturn(Optional.empty());
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        G4itRestException g4itRestException = assertThrows(G4itRestException.class, () ->
                administratorWorkspaceService.getUsersOfWorkspace(orgId, userBO)
        );

        assertEquals("Organization 12 not found.", g4itRestException.getMessage());
    }


    // Imports omitted for brevity

    @Test
    void getAllUsersOfOrg_AsSubscriberAdmin() {
        Workspace org = TestUtils.createOrganization();

        when(workspaceRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(org.getOrganization().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(org.getOrganization().getId()))).thenReturn(true);

        // Subscriber admin
        User adminUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userOrganizationRepository.findByOrganization(org.getOrganization())).thenReturn(List.of(userOrganization));

        // Org user
        User orgUser = User.builder().id(2L).email("org@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserWorkspace userWorkspace = UserWorkspace.builder().user(orgUser).roles(List.of(orgRole)).build();
        when(userWorkspaceRepository.findByWorkspace(org)).thenReturn(List.of(userWorkspace));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(org.getId(), userBO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 1L));
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void getNonSubscriberAdminUsersOfOrg_AsOrgAdmin() {
        Workspace org = TestUtils.createOrganization();

        when(workspaceRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(org.getOrganization().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(org.getOrganization().getId()))).thenReturn(false);

        User adminUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userOrganizationRepository.findByOrganization(org.getOrganization())).thenReturn(List.of(userOrganization));

        User orgUser = User.builder().id(2L).email("org@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        User userEntity = User.builder().id(3L).email("user@domain.com").build();
        Role userRole = Role.builder().name(Constants.ROLE_INVENTORY_READ).build();

        when(userWorkspaceRepository.findByWorkspace(org)).
                thenReturn(List.of(UserWorkspace.builder().user(orgUser).roles(List.of(orgRole)).build(),
                        UserWorkspace.builder().user(userEntity).roles(List.of(userRole)).build()));
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(org.getId(), userBO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
        assertTrue(result.stream().anyMatch(user -> user.getId() == 3L));
    }

    @Test
    void getUsersOfWorkspace_AsSuperAdmin() {
        Workspace org = TestUtils.createOrganization();

        when(workspaceRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(org.getOrganization().getId()), eq(org.getId()));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        userBO.setEmail(Constants.SUPER_ADMIN_EMAIL);
        // One org user
        User orgUser = User.builder().id(2L).email("user@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserWorkspace userWorkspace = UserWorkspace.builder().user(orgUser).roles(List.of(orgRole)).build();
        when(userWorkspaceRepository.findByWorkspace(org)).thenReturn(List.of(userWorkspace));
        when(userOrganizationRepository.findByOrganization(org.getOrganization())).thenReturn(Collections.emptyList());

        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(org.getId(), userBO);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void getUsersOfOrg_OrgUserIsSuperAdmin_SkippedInWorkspaceList() {
        Workspace org = TestUtils.createOrganization();

        when(workspaceRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(org.getOrganization().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(org.getOrganization().getId()))).thenReturn(false);

        // No subscriber admins
        when(userOrganizationRepository.findByOrganization(org.getOrganization())).thenReturn(Collections.emptyList());

        // Two org users, one has super admin email, one does not
        User superAdminUser = User.builder().id(11L).email(Constants.SUPER_ADMIN_EMAIL).build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserWorkspace userWorkspaceSuper = UserWorkspace.builder().user(superAdminUser).roles(List.of(orgRole)).build();

        User normalUser = User.builder().id(12L).email("user@domain.com").build();
        UserWorkspace userWorkspaceNormal = UserWorkspace.builder().user(normalUser).roles(List.of(orgRole)).build();

        when(userWorkspaceRepository.findByWorkspace(org)).thenReturn(List.of(userWorkspaceSuper, userWorkspaceNormal));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(org.getId(), userBO);

        // Should only return the normal user, not the super admin
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 12L));
    }

    @Test
    void getUsersOfOrg_OrgUserIsAlsoSubscriberAdmin_SubscriberAdminsFilteredFromWorkspaceList() {
        Workspace org = TestUtils.createOrganization();

        when(workspaceRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(org.getOrganization().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(org.getOrganization().getId()))).thenReturn(false);

        User sharedUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(sharedUser).roles(List.of(adminRole)).build();
        when(userOrganizationRepository.findByOrganization(org.getOrganization())).thenReturn(List.of(userOrganization));

        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserWorkspace userWorkspace = UserWorkspace.builder().user(sharedUser).roles(List.of(orgRole)).build();

        // Plus one more org user
        User orgUser2 = User.builder().id(2L).email("user@domain.com").build();
        UserWorkspace userWorkspace2 = UserWorkspace.builder().user(orgUser2).roles(List.of(orgRole)).build();

        when(userWorkspaceRepository.findByWorkspace(org)).thenReturn(List.of(userWorkspace, userWorkspace2));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(org.getId(), userBO);

        // Only the non-subscriber-admins should be returned
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void testDeleteUserOrgLink() {

        long userId = 1L;

        Workspace workspace = TestUtils.createOrganization();
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId,
                Collections.singletonList(userRoleRest));
        UserWorkspace userWorkspace = TestUtils.createUserOrganization(organizationId, userId);

        when(workspaceRepository.findById(organizationId)).thenReturn(Optional.of(workspace));
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(organizationId, userId))
                .thenReturn(java.util.Optional.of(userWorkspace));

        assertDoesNotThrow(() -> administratorWorkspaceService.deleteUserOrgLink(linkUserRoleRest, TestUtils.createUserBOAdminSub()));

        verify(userWorkspaceRepository, times(1)).deleteById(1L);
    }

}
