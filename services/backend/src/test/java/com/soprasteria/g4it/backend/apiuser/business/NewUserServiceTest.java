/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserSubscriberRepository userSubscriberRepository;

    @Mock
    private UserOrganizationRepository userOrganizationRepository;

    @Mock
    private UserRoleOrganizationRepository userRoleOrganizationRepository;

    @InjectMocks
    private NewUserService newUserService;

    @Test
    void createUserCreatesUserAndLinksWithSubscriberAndOrganization() {
        Subscriber subscriber = new Subscriber();
        Organization demoOrganization = new Organization();
        UserBO userInfo = UserBO.builder().email("email@example.com").firstName("First").lastName("Last").sub("sub").domain("domain").build();
        List<Role> accessRoles = List.of(Role.builder().name("ROLE_USER").build());
        User newUser = new User();

        when(userRepository.save(Mockito.any(User.class))).thenReturn(newUser);
        when(userOrganizationRepository.save(Mockito.any(UserOrganization.class))).thenReturn(new UserOrganization());

        User result = newUserService.createUser(subscriber, demoOrganization, null, userInfo, accessRoles);

        assertNotNull(result);
        verify(userSubscriberRepository).save(Mockito.any(UserSubscriber.class));
        verify(userOrganizationRepository).save(Mockito.any(UserOrganization.class));
        verify(userRoleOrganizationRepository).saveAll(anyList());
    }

    @Test
    void createUserReturnsExistingUserWhenProvided() {
        Subscriber subscriber = new Subscriber();
        Organization demoOrganization = new Organization();
        UserBO userInfo = UserBO.builder().email("email@example.com").firstName("First").lastName("Last").sub("sub").domain("domain").build();
        List<Role> accessRoles = List.of(Role.builder().name("ROLE_USER").build());
        User existingUser = new User();

        User result = newUserService.createUser(subscriber, demoOrganization, existingUser, userInfo, accessRoles);

        assertEquals(existingUser, result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void createNewUserCreatesUserWithoutRights() {
        UserBO userInfo = UserBO.builder().email("email@example.com").firstName("First").lastName("Last").sub("sub").domain("domain").build();

        User savedUser = new User();

        when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        User result = newUserService.createNewUser(userInfo);

        assertNotNull(result);
        assertEquals(savedUser, result);
        verify(userRepository).save(Mockito.any(User.class));
    }
}