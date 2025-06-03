/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * Administrator Organization service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AdministratorOrganizationService {
    @Autowired
    AdministratorRoleService administratorRoleService;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    RoleService roleService;
    @Autowired
    UserOrganizationRepository userOrganizationRepository;
    @Autowired
    UserSubscriberRepository userSubscriberRepository;
    @Autowired
    UserRoleOrganizationRepository userRoleOrganizationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRestMapper userRestMapper;
    @Autowired
    UserService userService;
    @Autowired
    AuthService authService;

    /**
     * Get the list of active organizations with admin role attached to subscriber
     * to be displayed in 'manage users' screen.
     * filter using subscriber or organization or both.
     *
     * @param subscriberId   the client subscriber id.
     * @param organizationId the organization id.
     * @param user           the user.
     * @return list of SubscriberBO.
     */
    public List<SubscriberBO> getOrganizations(final Long subscriberId, final Long organizationId, final UserBO user) {

        if (organizationId == null && subscriberId == null) {
            administratorRoleService.hasAdminRightsOnAnySubscriberOrAnyOrganization(user);
        }

        return user.getSubscribers().stream()
                .filter(subscriberBO -> subscriberId == null || Objects.equals(subscriberBO.getId(), subscriberId))
                .peek(subscriberBO -> {
                    final var organizations = subscriberBO.getOrganizations().stream()
                            .filter(organizationBO -> organizationId == null || Objects.equals(organizationBO.getId(), organizationId))
                            .filter(organizationBO -> Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS.contains(organizationBO.getStatus()))
                            .toList();
                    subscriberBO.setOrganizations(organizations);
                })
                .toList();

    }

    /**
     * Update the organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param user                   the user.
     * @return OrganizationBO
     */
    public OrganizationBO updateOrganization(final Long organizationId, final OrganizationUpsertRest organizationUpsertRest, UserBO user) {
        // Check Admin Role on this subscriber or organization.
        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, organizationUpsertRest.getSubscriberId(), organizationId);
        OrganizationBO organizationBO = organizationService.updateOrganization(organizationId, organizationUpsertRest, user.getId());
        userService.clearUserAllCache();
        return organizationBO;
    }

    /**
     * Create an Organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param user                   the user.
     * @return organization BO.
     */
    public OrganizationBO createOrganization(OrganizationUpsertRest organizationUpsertRest, UserBO user, boolean checkAdminRole) {
        Long subscriberId = organizationUpsertRest.getSubscriberId();

        // Check Admin Role on this subscriber.
        if (checkAdminRole) {
            administratorRoleService.hasAdminRightsOnSubscriber(user, subscriberId);
        }

        final OrganizationBO result = organizationService.createOrganization(organizationUpsertRest, user, subscriberId);
        userService.clearUserCache(user);
        if (!checkAdminRole) {
            UserRoleRest userRoleRest = UserRoleRest.builder().userId(user.getId()).roles(List.of("ROLE_ORGANIZATION_ADMINISTRATOR")).build();
            LinkUserRoleRest linkUserRoleRest = LinkUserRoleRest.builder().organizationId(result.getId()).users(Collections.singletonList(userRoleRest)).build();
            linkUserToOrg(linkUserRoleRest, authService.getUser(), false);
        }

        return result;
    }

    /**
     * Get list of all the users linked to an organization
     *
     * @param organizationId the organization id.
     * @param user           the current user
     */
    public List<UserInfoBO> getUsersOfOrg(Long organizationId, final UserBO user) {

        final Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("Organization %d not found.", organizationId)));

        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, organization.getSubscriber().getId(), organizationId);


        List<UserInfoBO> users = new ArrayList<>(userSubscriberRepository.findBySubscriber(organization.getSubscriber())).stream()
                .<UserInfoBO>map(userSubscriber -> {
                    List<Role> roles = userSubscriber.getRoles();
                    if (roles.stream().noneMatch(role -> role.getName().equals(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR))) {
                        return null;
                    }

                    User u = userSubscriber.getUser();
                    return UserInfoBO.builder()
                            .id(u.getId())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .email(u.getEmail())
                            .roles(roles.stream()
                                    .map(Role::getName)
                                    .filter(name -> name.equals(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR))
                                    .toList())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        List<Long> adminIds = users.stream()
                .map(UserInfoBO::getId)
                .toList();

        List<UserInfoBO> usersByOrganization = userOrganizationRepository.findByOrganization(organization).stream()
                .filter(userOrganization -> !adminIds.contains(userOrganization.getUser().getId()))
                .<UserInfoBO>map(userOrganization -> {
                    User u = userOrganization.getUser();
                    return UserInfoBO.builder()
                            .id(u.getId())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .email(u.getEmail())
                            .roles(userOrganization.getRoles().stream().map(Role::getName).toList())
                            .build();
                })
                .toList();

        return Stream.concat(users.stream(), usersByOrganization.stream()).toList();
    }


    /**
     * link user to an organization
     *
     * @param linkUserRoleRest the linkUserRoleRest
     * @param user             the user
     */
    public List<UserInfoBO> linkUserToOrg(final LinkUserRoleRest linkUserRoleRest, final UserBO user, boolean checkAdminRole) {

        Long organizationId = linkUserRoleRest.getOrganizationId();

        final Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("OrganizationId %s is not found in database", organizationId)));

        if (checkAdminRole) {
            administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, organization.getSubscriber().getId(), organizationId);
        }
        List<UserInfoBO> userInfoList = new ArrayList<>();

        List<Role> allRoles = roleService.getAllRoles();

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            User userEntity = userRepository.findById(userRoleRest.getUserId()).orElseThrow();

            Optional<UserOrganization> userOrganizationOptional = userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userRoleRest.getUserId());

            UserOrganization userOrganization;

            if (userOrganizationOptional.isEmpty()) {
                userOrganization = UserOrganization.builder().
                        organization(organization)
                        .user(userEntity)
                        .defaultFlag(true)
                        .build();

                userOrganizationRepository.save(userOrganization);
            } else {
                userOrganization = userOrganizationOptional.get();

                // delete linked roles from table g4it_user_role_organization if exist
                userRoleOrganizationRepository.deleteByUserOrganizations(userOrganization);
            }

            final List<Role> userRolesToAdd = userRoleRest.getRoles() == null ?
                    List.of() :
                    allRoles.stream()
                            .filter(role -> userRoleRest.getRoles().contains(role.getName()))
                            .toList();

            List<UserRoleOrganization> userRoleOrganizations = userRolesToAdd.stream()
                    .<UserRoleOrganization>map(role ->
                            UserRoleOrganization.builder()
                                    .userOrganizations(userOrganization)
                                    .roles(role)
                                    .build()
                    )
                    .toList();

            userRoleOrganizationRepository.saveAll(userRoleOrganizations);

            // Create and add UserInfoBO to the list
            userInfoList.add(UserInfoBO.builder()
                    .id(userRoleRest.getUserId())
                    .firstName(userEntity.getFirstName())
                    .lastName(userEntity.getLastName())
                    .email(userEntity.getEmail())
                    .roles(userRolesToAdd.stream().map(Role::getName).toList())
                    .build());


            userService.clearUserCache(userRestMapper.toBusinessObject(userEntity), organization.getSubscriber().getName(), organizationId);
        }

        return userInfoList;
    }

    /**
     * Delete user-organization link
     *
     * @param linkUserRoleRest the linkUserRoleRest
     * @param user             the  user
     */
    public void deleteUserOrgLink(final LinkUserRoleRest linkUserRoleRest, final UserBO user) {

        Long organizationId = linkUserRoleRest.getOrganizationId();
        Subscriber subscriber = organizationRepository.findById(organizationId).orElseThrow().getSubscriber();

        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, subscriber.getId(), organizationId);

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            UserOrganization userOrgEntity = userOrganizationRepository
                    .findByOrganizationIdAndUserId(organizationId, userRoleRest.getUserId()).orElseThrow();

            // delete linked roles from table g4it_user_role_organization if exist
            userRoleOrganizationRepository.deleteByUserOrganizations(userOrgEntity);

            // delete user-organization link
            userOrganizationRepository.deleteById(userOrgEntity.getId());

            userService.clearUserCache(userRestMapper.toBusinessObject(userOrgEntity.getUser()), subscriber.getName(), organizationId);
        }
    }

}
