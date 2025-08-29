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
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void givenEmail_thenReturnUser() {

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(User.builder().email(EMAIL)
                .userOrganizations(List.of(UserOrganization.builder()
                        .defaultFlag(true)
                        .roles(List.of(Role.builder().name("ROLE_SUBSCRIBER_ADMINISTRATOR").build()))
                        .organization(Organization.builder()
                                .workspaces(List.of(Workspace.builder()
                                        .status(WorkspaceStatus.ACTIVE.name())
                                        .build()))
                                .name(SUBSCRIBER)
                                .build())
                        .build()))
                .userWorkspaces(List.of(UserWorkspace
                        .builder().defaultFlag(true)
                        .roles(List.of(Role.builder().name("ROLE_INVENTORY_READ").build()))
                        .workspace(Workspace.builder().name(ORGANIZATION).status(WorkspaceStatus.ACTIVE.name())
                                .organization(Organization.builder().name(SUBSCRIBER).build()).build())
                        .build()))
                .build()));

        final UserBO user = userService.getUserByName(UserBO.builder().email(EMAIL).build());

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getOrganizations().getFirst().getRoles()).contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR);

        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    void givenEmailAndUserWithoutRole_thenReturnUserWithoutSubscriber() {

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(User.builder().email(EMAIL)
                .userOrganizations(List.of(UserOrganization.builder()
                        .defaultFlag(true)
                        .roles(List.of())
                        .organization(Organization.builder()
                                .workspaces(List.of(Workspace.builder()
                                        .status(WorkspaceStatus.ACTIVE.name())
                                        .build()))
                                .name(SUBSCRIBER)
                                .criteria(List.of("criteria"))
                                .build())
                        .build()))
                .userWorkspaces(List.of(UserWorkspace
                        .builder().defaultFlag(true)
                        .roles(List.of())
                        .workspace(Workspace.builder().name(ORGANIZATION).status(WorkspaceStatus.ACTIVE.name())
                                .organization(Organization.builder().name(SUBSCRIBER).build()).build())
                        .build()))
                .build()));

        final UserBO user = userService.getUserByName(UserBO.builder().email(EMAIL).build());

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getOrganizations()).isEmpty();

        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

}
