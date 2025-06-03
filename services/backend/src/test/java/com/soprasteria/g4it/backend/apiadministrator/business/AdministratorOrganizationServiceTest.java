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
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapperImpl;
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
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
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
    @Spy
    UserRestMapper userRestMapper = new UserRestMapperImpl();

    @Mock
    UserService userService;

/*    private final Organization organization = TestUtils.createOrganization();

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        organizationId = organization.getId();
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), any(), any());
    }*/

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

        SubscriberBO subscriberBO = result.get(0);
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
        OrganizationUpsertRest organizationUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        OrganizationBO updatedOrganization = OrganizationBO.builder().id(orgId).name("UpdatedName").build();

        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(userBO, subscriberId, orgId);
        when(organizationService.updateOrganization(orgId, organizationUpsertRest, userBO.getId())).thenReturn(updatedOrganization);

        OrganizationBO result = administratorOrganizationService.updateOrganization(orgId, organizationUpsertRest, userBO);

        verify(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(userBO, subscriberId, orgId);
        verify(organizationService).updateOrganization(orgId, organizationUpsertRest, userBO.getId());
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
        OrganizationUpsertRest organizationUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);

        doThrow(new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "User with id '1' do not have admin role on subscriber '1' or organization '1'")).when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(userBO, subscriberId, orgId);

        assertThrows(AuthorizationException.class, () -> {
            administratorOrganizationService.updateOrganization(orgId, organizationUpsertRest, userBO);
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
        OrganizationUpsertRest organizationUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        OrganizationBO expectedOrg = OrganizationBO.builder().id(33L).build();

        when(organizationService.createOrganization(organizationUpsertRest, userBO, subscriberId)).thenReturn(expectedOrg);

        OrganizationBO result = administratorOrganizationService.createOrganization(organizationUpsertRest, userBO, true);

        verify(administratorRoleService).hasAdminRightsOnSubscriber(userBO, subscriberId);
        verify(organizationService).createOrganization(organizationUpsertRest, userBO, subscriberId);
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
        OrganizationUpsertRest organizationUpsertRest =
                TestUtils.createOrganizationUpsert(subscriberId, organizationName, updatedStatus, dataRetentionDay);
        OrganizationBO expectedOrg = OrganizationBO.builder().id(1L).name(organizationName).build();

        Organization org = TestUtils.createOrganization();
        User userEntity = User.builder().id(1L).build();

        when(organizationService.createOrganization(organizationUpsertRest, userBO, subscriberId)).thenReturn(expectedOrg);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(roleService.getAllRoles()).thenReturn(List.of(Role.builder().name(ROLE).build()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userOrganizationRepository.findByOrganizationIdAndUserId(org.getId(), userBO.getId())).thenReturn(Optional.empty());

        OrganizationBO result = administratorOrganizationService.createOrganization(organizationUpsertRest, userBO, false);

        verify(organizationService).createOrganization(organizationUpsertRest, userBO, subscriberId);
        verify(userService).clearUserCache(userBO);
        verify(userRepository).findById(1L);
        verify(userOrganizationRepository).save(any());
        verify(userRoleOrganizationRepository).saveAll(any());

        assertEquals(expectedOrg, result);
    }

    @Test
    void getUsersOfOrg() {
        Organization org = TestUtils.createOrganization();

        when(organizationRepository.findById(org.getId())).thenReturn(Optional.of(org));
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), eq(org.getSubscriber().getId()), eq(org.getId()));

        User adminUser = User.builder().id(1L).build();
        Role adminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();

        UserSubscriber userSubscriber = UserSubscriber.builder().user(adminUser).roles(List.of(adminRole)).build();
        when(userSubscriberRepository.findBySubscriber(org.getSubscriber())).thenReturn(List.of(userSubscriber));

        User orgUser = User.builder().id(2L).build();
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
    void getUsersOfOrgNotFound() {
        Long orgId = 12L;
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        G4itRestException g4itRestException = assertThrows(G4itRestException.class, () ->
                administratorOrganizationService.getUsersOfOrg(orgId, TestUtils.createUserBO(List.of(ROLE)))
        );

        assertEquals("Organization 12 not found.", g4itRestException.getMessage());
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
