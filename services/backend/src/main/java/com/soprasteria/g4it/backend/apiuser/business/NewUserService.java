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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * New User Service
 */
@Service
public class NewUserService {

    /**
     * The user repository to access user data in database.
     */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserSubscriberRepository userSubscriberRepository;

    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    @Autowired
    private UserRoleOrganizationRepository userRoleOrganizationRepository;

    /**
     * Create the user and its related objects
     *
     * @param subscriber       the subscriber
     * @param demoWorkspace the demo organization
     * @param newUser          the new user
     * @param userInfo         the userInfo (email, firstName, lastname and subject)
     * @param accessRoles      the list of roles
     * @return the user created
     */
    public User createUser(final Subscriber subscriber,
                           final Workspace demoWorkspace,
                           User newUser, final UserBO userInfo,
                           final List<Role> accessRoles
    ) {

        if (newUser == null) {
            //add new user in g4it_user
            newUser = createNewUser(userInfo, subscriber, demoWorkspace, accessRoles);
        }

        if (accessRoles == null) return newUser;
        
        // Link user with subscriber
        userSubscriberRepository.save(UserSubscriber.builder()
                .user(newUser)
                .subscriber(subscriber)
                .defaultFlag(true)
                .build());

        //Link user with organization
        final UserOrganization userOrganization = userOrganizationRepository.save(UserOrganization.builder()
                .user(newUser)
                .workspace(demoWorkspace)
                .defaultFlag(true)
                .build());

        //give role access to the user
        userRoleOrganizationRepository.saveAll(accessRoles.stream()
                .map(role -> UserRoleOrganization.builder()
                        .userOrganizations(userOrganization)
                        .roles(role)
                        .build())
                .toList());

        return newUser;
    }

    /**
     * Create the user without any right
     *
     * @param userBO the user info
     * @return the user in database
     */
    public User createNewUser(final UserBO userBO) {
        return createNewUser(userBO, null, null, null);
    }

    /**
     * add new user in g4it_user
     *
     * @param userInfo    the userInfo
     * @param subscriber  the subscriber
     * @param demoOrg     the organization
     * @param accessRoles the access Roles
     * @return the user.
     */
    private User createNewUser(final UserBO userInfo, final Subscriber subscriber, final Workspace demoOrg, final List<Role> accessRoles) {
        User userToCreate = User.builder()
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .sub(userInfo.getSub())
                .domain(userInfo.getDomain())
                .creationDate(LocalDateTime.now())
                .build();

        if (subscriber != null && accessRoles != null) {
            userToCreate.setUserSubscribers(List.of(
                    UserSubscriber.builder()
                            .subscriber(subscriber)
                            .defaultFlag(true)
                            .build()));

            userToCreate.setUserOrganizations(List.of(UserOrganization.builder()
                    .workspace(demoOrg)
                    .defaultFlag(true)
                    .roles(accessRoles)
                    .build()));
        }

        return userRepository.save(userToCreate);
    }

}
