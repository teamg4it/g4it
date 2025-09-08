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
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapperImpl;
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
import org.mockito.Spy;
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

    private long workspaceId;

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
    @Spy
    UserRestMapper userRestMapper = new UserRestMapperImpl();
    @Mock
    UserService userService;

    @Test
    void linkUserToWorkspace_WithRoles() {

        long userId = 1L;

        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));


        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(workspaceId,
                Collections.singletonList(userRoleRest));

        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(workspaceId, userId)).thenReturn(Optional.empty());
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(TestUtils.createWorkspace()));
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
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(workspaceId, Collections.singletonList(userRoleRest));

        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(workspaceId, userRoleRest.getUserId())).thenReturn(Optional.empty());
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(TestUtils.createWorkspace()));
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
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(workspaceId,
                Collections.singletonList(userRoleRest));
        UserWorkspace userWorkspace = TestUtils.createUserWorkspace(workspaceId, userId);

        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(workspaceId, userRoleRest.getUserId())).thenReturn(Optional.ofNullable(userWorkspace));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(TestUtils.createWorkspace()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorWorkspaceService.linkUserToWorkspace(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(ROLE, users.getFirst().getRoles().getFirst());
        verify(userRoleWorkspaceRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getWorkspacesWithOrganizationAndWorkspaceNull() {
        Long organizationId = null;
        Long workId = null;
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));

        doNothing().when(administratorRoleService).hasAdminRightsOnAnyOrganizationOrAnyWorkspace(userBO);

        List<OrganizationBO> result = administratorWorkspaceService.getWorkspaces(organizationId, workId, userBO);

        verify(administratorRoleService).hasAdminRightsOnAnyOrganizationOrAnyWorkspace(userBO);
        assertEquals(1, result.size());

        OrganizationBO organizationBO = result.getFirst();
        assertEquals(1, organizationBO.getWorkspaces().size());
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
        Long organizationId = 1L;
        Long workId = 33L;
        String workspaceName = "WORKSPACE";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(organizationId, workspaceName, updatedStatus, dataRetentionDay);
        WorkspaceBO updatedOrganization = WorkspaceBO.builder().id(workId).name("UpdatedName").build();

        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(userBO, organizationId, workId);
        when(workspaceService.updateWorkspace(workId, workspaceUpsertRest, userBO.getId())).thenReturn(updatedOrganization);

        WorkspaceBO result = administratorWorkspaceService.updateWorkspace(workId, workspaceUpsertRest, userBO);

        verify(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(userBO, organizationId, workId);
        verify(workspaceService).updateWorkspace(workId, workspaceUpsertRest, userBO.getId());
        verify(userService).clearUserAllCache();

        assertEquals(workId, result.getId());
        assertEquals("UpdatedName", result.getName());
    }

    @Test
    void updateWorkspaceNoAdminRights() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long organizationId = 1L;
        Long workId = 33L;
        String workspaceName = "WORKSPACE";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(organizationId, workspaceName, updatedStatus, dataRetentionDay);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "User with id '1' do not have admin role on organization '1' or organization '1'")).when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(userBO, organizationId, workId);

        assertThrows(AuthorizationException.class, () -> {
            administratorWorkspaceService.updateWorkspace(workId, workspaceUpsertRest, userBO);
        });

        verify(workspaceService, never()).updateWorkspace(any(), any(), any());
        verify(userService, never()).clearUserAllCache();
    }

    @Test
    void createWorkspaceWithAdminRole() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long organizationId = 1L;
        String workspaceName = "WORKSPACE";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(organizationId, workspaceName, updatedStatus, dataRetentionDay);
        WorkspaceBO expectedOrg = WorkspaceBO.builder().id(33L).build();

        when(workspaceService.createWorkspace(workspaceUpsertRest, userBO, organizationId)).thenReturn(expectedOrg);
        when(roleService.isUserDomainAuthorized(userBO, organizationId)).thenReturn(true);
        when(roleService.hasAdminRightsOnOrganization(userBO, organizationId)).thenReturn(true);
        WorkspaceBO result = administratorWorkspaceService.createWorkspace(workspaceUpsertRest, userBO, true);

        verify(administratorRoleService).hasOrganizationAdminOrDomainAccess(userBO, organizationId, true, true);
        verify(workspaceService).createWorkspace(workspaceUpsertRest, userBO, organizationId);
        verify(userService).clearUserCache(userBO);

        assertEquals(expectedOrg, result);
    }

    @Test
    void createWorkspaceWithoutAdminRole() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long organizationId = 1L;
        String workspaceName = "WORKSPACE";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(organizationId, workspaceName, updatedStatus, dataRetentionDay);
        WorkspaceBO expectedOrg = WorkspaceBO.builder().id(1L).name(workspaceName).build();

        Workspace work = TestUtils.createWorkspace();
        User userEntity = User.builder().id(1L).build();

        when(workspaceService.createWorkspace(workspaceUpsertRest, userBO, organizationId)).thenReturn(expectedOrg);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(work));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(work.getId(), userBO.getId())).thenReturn(Optional.empty());
        when(roleService.isUserDomainAuthorized(userBO, organizationId)).thenReturn(true);
        when(roleService.hasAdminRightsOnOrganization(userBO, organizationId)).thenReturn(false);

        WorkspaceBO result = administratorWorkspaceService.createWorkspace(workspaceUpsertRest, userBO, false);

        verify(workspaceService).createWorkspace(workspaceUpsertRest, userBO, organizationId);
        verify(roleService).hasAdminRightsOnOrganization(userBO, organizationId);
        verify(userService).clearUserCache(userBO);
        verify(userRepository).findById(1L);
        verify(userWorkspaceRepository).save(any());
        verify(userRoleWorkspaceRepository).saveAll(any());

        assertEquals(expectedOrg, result);
    }

    @Test
    void createWorkspace_NoAdminRightsAnd_NoDomainAuthorization_ThrowException() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long organizationId = 1L;
        String workspaceName = "WORKSPACE";
        String updatedStatus = WorkspaceStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;

        WorkspaceUpsertRest workspaceUpsertRest =
                TestUtils.createOrganizationUpsert(organizationId, workspaceName, updatedStatus, dataRetentionDay);

        when(roleService.hasAdminRightsOnOrganization(userBO, organizationId)).thenReturn(false);
        when(roleService.isUserDomainAuthorized(userBO, organizationId)).thenReturn(false);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN,
                "User with id 1 has no admin role on organization 1 or has domain not authorized."))
                .when(administratorRoleService).hasOrganizationAdminOrDomainAccess(userBO,
                        organizationId, false, false);

        assertThrows(AuthorizationException.class, () -> {
            administratorWorkspaceService.createWorkspace(workspaceUpsertRest, userBO, true);
        });

        verify(roleService).hasAdminRightsOnOrganization(userBO, organizationId);
        verify(roleService).isUserDomainAuthorized(userBO, organizationId);
    }

    @Test
    void getUsersOfWorkspaceNotFound() {
        Long workId = 12L;
        when(workspaceRepository.findById(workId)).thenReturn(Optional.empty());
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        G4itRestException g4itRestException = assertThrows(G4itRestException.class, () ->
                administratorWorkspaceService.getUsersOfWorkspace(workId, userBO)
        );

        assertEquals("Workspace 12 not found.", g4itRestException.getMessage());
    }


    // Imports omitted for brevity

    @Test
    void getAllUsersOfOrg_AsOrganizationAdmin() {
        Workspace work = TestUtils.createWorkspace();

        when(workspaceRepository.findById(work.getId())).thenReturn(Optional.of(work));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(work.getOrganization().getId()), eq(work.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(work.getOrganization().getId()))).thenReturn(true);

        // Organization admin
        User adminUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userOrganizationRepository.findByOrganization(work.getOrganization())).thenReturn(List.of(userOrganization));

        // Org user
        User orgUser = User.builder().id(2L).email("work@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_WORKSPACE_ADMINISTRATOR).build();
        UserWorkspace userWorkspace = UserWorkspace.builder().user(orgUser).roles(List.of(orgRole)).build();
        when(userWorkspaceRepository.findByWorkspace(work)).thenReturn(List.of(userWorkspace));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(work.getId(), userBO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 1L));
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void getNonOrganizationAdminUsersOfOrg_AsOrgAdmin() {
        Workspace work = TestUtils.createWorkspace();

        when(workspaceRepository.findById(work.getId())).thenReturn(Optional.of(work));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(work.getOrganization().getId()), eq(work.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(work.getOrganization().getId()))).thenReturn(false);

        User adminUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userOrganizationRepository.findByOrganization(work.getOrganization())).thenReturn(List.of(userOrganization));

        User orgUser = User.builder().id(2L).email("work@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_WORKSPACE_ADMINISTRATOR).build();
        User userEntity = User.builder().id(3L).email("user@domain.com").build();
        Role userRole = Role.builder().name(Constants.ROLE_INVENTORY_READ).build();

        when(userWorkspaceRepository.findByWorkspace(work)).
                thenReturn(List.of(UserWorkspace.builder().user(orgUser).roles(List.of(orgRole)).build(),
                        UserWorkspace.builder().user(userEntity).roles(List.of(userRole)).build()));
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(work.getId(), userBO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
        assertTrue(result.stream().anyMatch(user -> user.getId() == 3L));
    }

    @Test
    void getUsersOfWorkspace_AsSuperAdmin() {
        Workspace work = TestUtils.createWorkspace();

        when(workspaceRepository.findById(work.getId())).thenReturn(Optional.of(work));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(work.getOrganization().getId()), eq(work.getId()));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        userBO.setEmail(Constants.SUPER_ADMIN_EMAIL);
        // One work user
        User orgUser = User.builder().id(2L).email("user@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_WORKSPACE_ADMINISTRATOR).build();
        UserWorkspace userWorkspace = UserWorkspace.builder().user(orgUser).roles(List.of(orgRole)).build();
        when(userWorkspaceRepository.findByWorkspace(work)).thenReturn(List.of(userWorkspace));
        when(userOrganizationRepository.findByOrganization(work.getOrganization())).thenReturn(Collections.emptyList());

        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(work.getId(), userBO);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void getUsersOfOrg_OrgUserIsSuperAdmin_SkippedInWorkspaceList() {
        Workspace work = TestUtils.createWorkspace();

        when(workspaceRepository.findById(work.getId())).thenReturn(Optional.of(work));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(work.getOrganization().getId()), eq(work.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(work.getOrganization().getId()))).thenReturn(false);

        // No organization admins
        when(userOrganizationRepository.findByOrganization(work.getOrganization())).thenReturn(Collections.emptyList());

        // Two work users, one has super admin email, one does not
        User superAdminUser = User.builder().id(11L).email(Constants.SUPER_ADMIN_EMAIL).build();
        Role orgRole = Role.builder().name(Constants.ROLE_WORKSPACE_ADMINISTRATOR).build();
        UserWorkspace userWorkspaceSuper = UserWorkspace.builder().user(superAdminUser).roles(List.of(orgRole)).build();

        User normalUser = User.builder().id(12L).email("user@domain.com").build();
        UserWorkspace userWorkspaceNormal = UserWorkspace.builder().user(normalUser).roles(List.of(orgRole)).build();

        when(userWorkspaceRepository.findByWorkspace(work)).thenReturn(List.of(userWorkspaceSuper, userWorkspaceNormal));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(work.getId(), userBO);

        // Should only return the normal user, not the super admin
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 12L));
    }

    @Test
    void getUsersOfOrg_OrgUserIsAlsoOrganizationAdmin_OrganizationAdminsFilteredFromWorkspaceList() {
        Workspace work = TestUtils.createWorkspace();

        when(workspaceRepository.findById(work.getId())).thenReturn(Optional.of(work));
        doNothing().when(administratorRoleService).hasAdminRightOnOrganizationOrWorkspace(any(), eq(work.getOrganization().getId()), eq(work.getId()));
        when(roleService.hasAdminRightsOnOrganization(any(), eq(work.getOrganization().getId()))).thenReturn(false);

        User sharedUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(sharedUser).roles(List.of(adminRole)).build();
        when(userOrganizationRepository.findByOrganization(work.getOrganization())).thenReturn(List.of(userOrganization));

        Role orgRole = Role.builder().name(Constants.ROLE_WORKSPACE_ADMINISTRATOR).build();
        UserWorkspace userWorkspace = UserWorkspace.builder().user(sharedUser).roles(List.of(orgRole)).build();

        // Plus one more work user
        User orgUser2 = User.builder().id(2L).email("user@domain.com").build();
        UserWorkspace userWorkspace2 = UserWorkspace.builder().user(orgUser2).roles(List.of(orgRole)).build();

        when(userWorkspaceRepository.findByWorkspace(work)).thenReturn(List.of(userWorkspace, userWorkspace2));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorWorkspaceService.getUsersOfWorkspace(work.getId(), userBO);

        // Only the non-organization-admins should be returned
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void testDeleteUserOrgLink() {

        long userId = 1L;

        Workspace workspace = TestUtils.createWorkspace();
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(workspaceId,
                Collections.singletonList(userRoleRest));
        UserWorkspace userWorkspace = TestUtils.createUserWorkspace(workspaceId, userId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userWorkspaceRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(java.util.Optional.of(userWorkspace));

        assertDoesNotThrow(() -> administratorWorkspaceService.deleteUserWorkLink(linkUserRoleRest, TestUtils.createUserBOAdminSub()));

        verify(userWorkspaceRepository, times(1)).deleteById(1L);
    }

}
