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
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpdateRest;
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
class AdministratorOrganizationServiceTest {

    @InjectMocks
    private AdministratorOrganizationService administratorOrganizationService;

    private long organizationId;

    @Mock
    UserRepository userRepository;

    @Mock
    UserOrganizationRepository userOrganizationRepository;

    @Mock
    OrganizationRepository organizationRepository;

    @Mock
    UserRoleOrganizationRepository userRoleOrganizationRepository;

    @Mock
    private AdministratorRoleService administratorRoleService;
    @Mock
    private RoleService roleService;
    @Mock
    private OrganizationService organizationService;

    @Mock
    private AuthService authService;

    @Mock
    UserSubscriberRepository userSubscriberRepository;
    
    @Mock
    UserService userService;

    @Test
    void linkUserToOrg_WithRoles() {

        long userId = 1L;

        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));


        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId,
                Collections.singletonList(userRoleRest));

        when(userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userId)).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(TestUtils.createOrganization()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorOrganizationService.linkUserToOrg(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(ROLE, users.getFirst().getRoles().getFirst());
        verify(userOrganizationRepository, times(1)).save(any(UserOrganization.class));
        verify(userRoleOrganizationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void linkUserToOrg_WithoutRoles() {

        long userId = 1L;
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of());
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId, Collections.singletonList(userRoleRest));

        when(userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userRoleRest.getUserId())).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(TestUtils.createOrganization()));
        when(userRepository.findById(userRoleRest.getUserId())).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorOrganizationService.linkUserToOrg(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(List.of(), users.getFirst().getRoles());
        verify(userOrganizationRepository, times(1)).save(any(UserOrganization.class));
    }

    @Test
    void linkUserToOrg_NotEmpty() {

        long userId = 1L;

        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId,
                Collections.singletonList(userRoleRest));
        UserOrganization userOrganization = TestUtils.createUserOrganization(organizationId, userId);

        when(userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userRoleRest.getUserId())).thenReturn(Optional.ofNullable(userOrganization));
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(TestUtils.createOrganization()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));

        List<UserInfoBO> users = administratorOrganizationService.linkUserToOrg(linkUserRoleRest, TestUtils.createUserBOAdminSub(), true);
        assertEquals(1, users.size());
        assertEquals(ROLE, users.getFirst().getRoles().getFirst());
        verify(userRoleOrganizationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getOrganizationsWithSubscriberAndOrganizationNull() {
        Long subscriberId = null;
        Long orgId = null;
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));

        doNothing().when(administratorRoleService).hasAdminRightsOnAnySubscriberOrAnyOrganization(userBO);

        List<SubscriberBO> result = administratorOrganizationService.getOrganizations(subscriberId, orgId, userBO);

        verify(administratorRoleService).hasAdminRightsOnAnySubscriberOrAnyOrganization(userBO);
        assertEquals(1, result.size());

        SubscriberBO subscriberBO = result.getFirst();
        assertEquals(1, subscriberBO.getOrganizations().size());
    }

    @Test
    void getOrganizationsWithoutAdminRights() {
        UserBO userBO = UserBO.builder().id(1L).build();

        doThrow(new AuthorizationException(403, "User with id '1' do not have admin role")).when(administratorRoleService).hasAdminRightsOnAnySubscriberOrAnyOrganization(userBO);

        AuthorizationException exception = assertThrows(AuthorizationException.class, () -> {
            administratorOrganizationService.getOrganizations(null, null, userBO);
        });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("do not have admin role"));
    }

    @Test
    void updateOrganization() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        Long orgId = 33L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpdateRest workspaceUpdateRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        OrganizationBO updatedOrganization = OrganizationBO.builder().id(orgId).name("UpdatedName").build();

        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(userBO, subscriberId, orgId);
        when(organizationService.updateOrganization(orgId, workspaceUpdateRest, userBO.getId())).thenReturn(updatedOrganization);

        OrganizationBO result = administratorOrganizationService.updateOrganization(orgId, workspaceUpdateRest, userBO);

        verify(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(userBO, subscriberId, orgId);
        verify(organizationService).updateOrganization(orgId, workspaceUpdateRest, userBO.getId());
        verify(userService).clearUserAllCache();

        assertEquals(orgId, result.getId());
        assertEquals("UpdatedName", result.getName());
    }

    @Test
    void updateOrganizationNoAdminRights() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        Long orgId = 33L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpdateRest workspaceUpdateRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "User with id '1' do not have admin role on subscriber '1' or organization '1'")).when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(userBO, subscriberId, orgId);

        assertThrows(AuthorizationException.class, () -> {
            administratorOrganizationService.updateOrganization(orgId, workspaceUpdateRest, userBO);
        });

        verify(organizationService, never()).updateOrganization(any(), any(), any());
        verify(userService, never()).clearUserAllCache();
    }

    @Test
    void createOrganizationWithAdminRole() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpdateRest workspaceUpdateRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        OrganizationBO expectedOrg = OrganizationBO.builder().id(33L).build();

        when(organizationService.createOrganization(workspaceUpdateRest, userBO, subscriberId)).thenReturn(expectedOrg);
        when(roleService.isUserDomainAuthorized(userBO, subscriberId)).thenReturn(true);
        when(roleService.hasAdminRightsOnSubscriber(userBO, subscriberId)).thenReturn(true);
        OrganizationBO result = administratorOrganizationService.createOrganization(workspaceUpdateRest, userBO, true);

        verify(administratorRoleService).hasSubscriberAdminOrDomainAccess(userBO, subscriberId, true, true);
        verify(organizationService).createOrganization(workspaceUpdateRest, userBO, subscriberId);
        verify(userService).clearUserCache(userBO);

        assertEquals(expectedOrg, result);
    }

    @Test
    void createOrganizationWithoutAdminRole() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;
        WorkspaceUpdateRest workspaceUpdateRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        OrganizationBO expectedOrg = OrganizationBO.builder().id(1L).name(organizationName).build();

        Organization org = TestUtils.createOrganization();
        User userEntity = User.builder().id(1L).build();

        when(organizationService.createOrganization(workspaceUpdateRest, userBO, subscriberId)).thenReturn(expectedOrg);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userOrganizationRepository.findByOrganizationIdAndUserId(org.getId(), userBO.getId())).thenReturn(Optional.empty());
        when(roleService.isUserDomainAuthorized(userBO, subscriberId)).thenReturn(true);
        when(roleService.hasAdminRightsOnSubscriber(userBO, subscriberId)).thenReturn(false);

        OrganizationBO result = administratorOrganizationService.createOrganization(workspaceUpdateRest, userBO, false);

        verify(organizationService).createOrganization(workspaceUpdateRest, userBO, subscriberId);
        verify(roleService).hasAdminRightsOnSubscriber(userBO, subscriberId);
        verify(userService).clearUserCache(userBO);
        verify(userRepository).findById(1L);
        verify(userOrganizationRepository).save(any());
        verify(userRoleOrganizationRepository).saveAll(any());

        assertEquals(expectedOrg, result);
    }

    @Test
    void createOrganization_NoAdminRightsAnd_NoDomainAuthorization_ThrowException() {
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        Long subscriberId = 1L;
        String organizationName = "ORGANIZATION";
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        long dataRetentionDay = 7L;

        WorkspaceUpdateRest workspaceUpdateRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);

        when(roleService.hasAdminRightsOnSubscriber(userBO, subscriberId)).thenReturn(false);
        when(roleService.isUserDomainAuthorized(userBO, subscriberId)).thenReturn(false);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN,
                "User with id 1 has no admin role on subscriber 1 or has domain not authorized."))
                .when(administratorRoleService).hasSubscriberAdminOrDomainAccess(userBO,
                        subscriberId, false, false);

        assertThrows(AuthorizationException.class, () -> {
            administratorOrganizationService.createOrganization(workspaceUpdateRest, userBO, true);
        });

        verify(roleService).hasAdminRightsOnSubscriber(userBO, subscriberId);
        verify(roleService).isUserDomainAuthorized(userBO, subscriberId);
    }

    @Test
    void getUsersOfOrgNotFound() {
        Long orgId = 12L;
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());
        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        G4itRestException g4itRestException = assertThrows(G4itRestException.class, () ->
                administratorOrganizationService.getUsersOfOrg(orgId, userBO)
        );

        assertEquals("Organization 12 not found.", g4itRestException.getMessage());
    }


    // Imports omitted for brevity

    @Test
    void getAllUsersOfOrg_AsSubscriberAdmin() {
        Organization org = TestUtils.createOrganization();

        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), eq(org.getSubscriber().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnSubscriber(any(), eq(org.getSubscriber().getId()))).thenReturn(true);

        // Subscriber admin
        User adminUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
        UserSubscriber userSubscriber = UserSubscriber.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userSubscriberRepository.findBySubscriber(org.getSubscriber())).thenReturn(List.of(userSubscriber));

        // Org user
        User orgUser = User.builder().id(2L).email("org@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(orgUser).roles(List.of(orgRole)).build();
        when(userOrganizationRepository.findByOrganization(org)).thenReturn(List.of(userOrganization));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorOrganizationService.getUsersOfOrg(org.getId(), userBO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 1L));
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void getNonSubscriberAdminUsersOfOrg_AsOrgAdmin() {
        Organization org = TestUtils.createOrganization();

        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), eq(org.getSubscriber().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnSubscriber(any(), eq(org.getSubscriber().getId()))).thenReturn(false);

        User adminUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
        UserSubscriber userSubscriber = UserSubscriber.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userSubscriberRepository.findBySubscriber(org.getSubscriber())).thenReturn(List.of(userSubscriber));

        User orgUser = User.builder().id(2L).email("org@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        User userEntity = User.builder().id(3L).email("user@domain.com").build();
        Role userRole = Role.builder().name(Constants.ROLE_INVENTORY_READ).build();

        when(userOrganizationRepository.findByOrganization(org)).
                thenReturn(List.of(UserOrganization.builder().user(orgUser).roles(List.of(orgRole)).build(),
                        UserOrganization.builder().user(userEntity).roles(List.of(userRole)).build()));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorOrganizationService.getUsersOfOrg(org.getId(), userBO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
        assertTrue(result.stream().anyMatch(user -> user.getId() == 3L));
    }

    @Test
    void getUsersOfOrg_AsSuperAdmin() {
        Organization org = TestUtils.createOrganization();

        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), eq(org.getSubscriber().getId()), eq(org.getId()));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        userBO.setEmail(Constants.SUPER_ADMIN_EMAIL);
        // One org user
        User orgUser = User.builder().id(2L).email("user@domain.com").build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(orgUser).roles(List.of(orgRole)).build();
        when(userOrganizationRepository.findByOrganization(org)).thenReturn(List.of(userOrganization));
        when(userSubscriberRepository.findBySubscriber(org.getSubscriber())).thenReturn(Collections.emptyList());

        List<UserInfoBO> result = administratorOrganizationService.getUsersOfOrg(org.getId(), userBO);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void getUsersOfOrg_OrgUserIsSuperAdmin_SkippedInOrgList() {
        Organization org = TestUtils.createOrganization();

        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), eq(org.getSubscriber().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnSubscriber(any(), eq(org.getSubscriber().getId()))).thenReturn(false);

        // No subscriber admins
        when(userSubscriberRepository.findBySubscriber(org.getSubscriber())).thenReturn(Collections.emptyList());

        // Two org users, one has super admin email, one does not
        User superAdminUser = User.builder().id(11L).email(Constants.SUPER_ADMIN_EMAIL).build();
        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganizationSuper = UserOrganization.builder().user(superAdminUser).roles(List.of(orgRole)).build();

        User normalUser = User.builder().id(12L).email("user@domain.com").build();
        UserOrganization userOrganizationNormal = UserOrganization.builder().user(normalUser).roles(List.of(orgRole)).build();

        when(userOrganizationRepository.findByOrganization(org)).thenReturn(List.of(userOrganizationSuper, userOrganizationNormal));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorOrganizationService.getUsersOfOrg(org.getId(), userBO);

        // Should only return the normal user, not the super admin
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 12L));
    }

    @Test
    void getUsersOfOrg_OrgUserIsAlsoSubscriberAdmin_SubscriberAdminsFilteredFromOrgList() {
        Organization org = TestUtils.createOrganization();

        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), eq(org.getSubscriber().getId()), eq(org.getId()));
        when(roleService.hasAdminRightsOnSubscriber(any(), eq(org.getSubscriber().getId()))).thenReturn(false);

        User sharedUser = User.builder().id(1L).email("admin@domain.com").build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
        UserSubscriber userSubscriber = UserSubscriber.builder().user(sharedUser).roles(List.of(adminRole)).build();
        when(userSubscriberRepository.findBySubscriber(org.getSubscriber())).thenReturn(List.of(userSubscriber));

        Role orgRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
        UserOrganization userOrganization = UserOrganization.builder().user(sharedUser).roles(List.of(orgRole)).build();

        // Plus one more org user
        User orgUser2 = User.builder().id(2L).email("user@domain.com").build();
        UserOrganization userOrganization2 = UserOrganization.builder().user(orgUser2).roles(List.of(orgRole)).build();

        when(userOrganizationRepository.findByOrganization(org)).thenReturn(List.of(userOrganization, userOrganization2));

        UserBO userBO = TestUtils.createUserBO(List.of(ROLE));
        List<UserInfoBO> result = administratorOrganizationService.getUsersOfOrg(org.getId(), userBO);

        // Only the non-subscriber-admins should be returned
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(user -> user.getId() == 2L));
    }

    @Test
    void testDeleteUserOrgLink() {

        long userId = 1L;

        Organization organization = TestUtils.createOrganization();
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of(ROLE));
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId,
                Collections.singletonList(userRoleRest));
        UserOrganization userOrganization = TestUtils.createUserOrganization(organizationId, userId);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(java.util.Optional.of(userOrganization));

        assertDoesNotThrow(() -> administratorOrganizationService.deleteUserOrgLink(linkUserRoleRest, TestUtils.createUserBOAdminSub()));

        verify(userOrganizationRepository, times(1)).deleteById(1L);
    }

}
