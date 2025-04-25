/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapperImpl;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleOrganizationRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.ROLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Spy
    UserRestMapper userRestMapper = new UserRestMapperImpl();

    @Mock
    UserService userService;

    private final Organization organization = TestUtils.createOrganization();

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        organizationId = organization.getId();
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), any(), any());
    }

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
