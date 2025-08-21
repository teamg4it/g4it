/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
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
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpdateRest;
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
public class AdministratorWorkspaceService {
    @Autowired
    AdministratorRoleService administratorRoleService;
    @Autowired
    WorkspaceService workspaceService;
    @Autowired
    WorkspaceRepository workspaceRepository;
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
     * @param subscriberId the client subscriber id.
     * @param workspaceId  the organization id.
     * @param user         the user.
     * @return list of SubscriberBO.
     */
    public List<SubscriberBO> getWorkspaces(final Long subscriberId, final Long workspaceId, final UserBO user) {

        if (workspaceId == null && subscriberId == null) {
            administratorRoleService.hasAdminRightsOnAnySubscriberOrAnyOrganization(user);
        }

        return user.getSubscribers().stream()
                .filter(subscriberBO -> subscriberId == null || Objects.equals(subscriberBO.getId(), subscriberId))
                .peek(subscriberBO -> {
                    final var workspaces = subscriberBO.getOrganizations().stream()
                            .filter(organizationBO -> workspaceId == null || Objects.equals(organizationBO.getId(), workspaceId))
                            .filter(organizationBO -> Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS.contains(organizationBO.getStatus()))
                            .toList();
                    subscriberBO.setOrganizations(workspaces);
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
    public OrganizationBO updateWorkspace(final Long workspaceId, final WorkspaceUpdateRest organizationUpsertRest, UserBO user) {
        // Check Admin Role on this subscriber or organization.
        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, organizationUpsertRest.getWorkspaceId(), workspaceId);
        OrganizationBO organizationBO = workspaceService.updateWorkspace(workspaceId, organizationUpsertRest, user.getId());
        userService.clearUserAllCache();
        return organizationBO;
    }

    /**
     * Create an Organization.
     *
     * @param workspaceUpdateRest the WorkspaceUpdateRest.
     * @param user                the user.
     * @return organization BO.
     */
    public OrganizationBO createWorkspace(WorkspaceUpdateRest workspaceUpdateRest, UserBO user, boolean checkAdminRole) {
        Long subscriberId = workspaceUpdateRest.getWorkspaceId();
        boolean hasSubscriberAdminRights = roleService.hasAdminRightsOnSubscriber(user, subscriberId);
        boolean hasDomainAuthorization = roleService.isUserDomainAuthorized(user, subscriberId);

        // Check Admin Role on this subscriber or the logged-in org admin user's domain is authorized
        if (checkAdminRole) {
            administratorRoleService.hasSubscriberAdminOrDomainAccess(user, subscriberId, hasSubscriberAdminRights,
                    hasDomainAuthorization);
        }

        final OrganizationBO result = workspaceService.createWorkspace(workspaceUpdateRest, user, subscriberId);
        userService.clearUserCache(user);

        if (hasSubscriberAdminRights)
            return result;

        // Link user to the organization and assign with org admin role
        if (!(checkAdminRole) || hasDomainAuthorization) {
            UserRoleRest userRoleRest = UserRoleRest.builder().userId(user.getId()).roles(List.of("ROLE_ORGANIZATION_ADMINISTRATOR")).build();
            LinkUserRoleRest linkUserRoleRest = LinkUserRoleRest.builder().workspaceId(result.getId()).users(Collections.singletonList(userRoleRest)).build();
            linkUserToWorkspace(linkUserRoleRest, authService.getUser(), false);
        }

        return result;
    }

    /**
     * Get list of all the users linked to an organization
     *
     * @param workspaceId the organization id.
     * @param user        the current user
     */
    public List<UserInfoBO> getUsersOfWorkspace(Long workspaceId, final UserBO user) {
        final Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new G4itRestException("404", String.format("Organization %d not found.", workspaceId)));
        long subscriberId = workspace.getSubscriber().getId();
        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, subscriberId, workspaceId);

        // fetch subscriber admins
        List<UserInfoBO> subscriberAdmins = new ArrayList<>(userSubscriberRepository.findBySubscriber(workspace.getSubscriber())).stream()
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

        List<Long> subAdminIds = subscriberAdmins.stream()
                .map(UserInfoBO::getId)
                .toList();

        List<UserInfoBO> usersByOrganization = userOrganizationRepository.findByWorkspace(workspace).stream()
                .filter(userOrganization -> !subAdminIds.contains(userOrganization.getUser().getId()))
                .filter(userOrganization -> !Constants.SUPER_ADMIN_EMAIL.equalsIgnoreCase(userOrganization.getUser().getEmail()))
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

        // Retrieve all users, including subscriber admins, when the logged-in user is a subscriber admin
        if (roleService.hasAdminRightsOnSubscriber(user, subscriberId)) {
            return Stream.concat(subscriberAdmins.stream(), usersByOrganization.stream()).toList();
        }
        //  Return the users except for subscriber admins when the logged-in user is an organization admin.
        else {
            return usersByOrganization;
        }
    }


    /**
     * link user to an organization
     *
     * @param linkUserRoleRest the linkUserRoleRest
     * @param user             the user
     */
    public List<UserInfoBO> linkUserToWorkspace(final LinkUserRoleRest linkUserRoleRest, final UserBO user, boolean checkAdminRole) {

        Long organizationId = linkUserRoleRest.getWorkspaceId();

        final Workspace workspace = workspaceRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("OrganizationId %s is not found in database", organizationId)));

        if (checkAdminRole) {
            administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, workspace.getSubscriber().getId(), organizationId);
        }
        List<UserInfoBO> userInfoList = new ArrayList<>();

        List<Role> allRoles = roleService.getAllRoles();

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            User userEntity = userRepository.findById(userRoleRest.getUserId()).orElseThrow();

            Optional<UserOrganization> userOrganizationOptional = userOrganizationRepository.findByWorkspaceIdAndUserId(organizationId, userRoleRest.getUserId());

            UserOrganization userOrganization;

            if (userOrganizationOptional.isEmpty()) {
                userOrganization = UserOrganization.builder().
                        workspace(workspace)
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


            userService.clearUserCache(userRestMapper.toBusinessObject(userEntity), workspace.getSubscriber().getName(), organizationId);
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

        Long organizationId = linkUserRoleRest.getWorkspaceId();
        Subscriber subscriber = workspaceRepository.findById(organizationId).orElseThrow().getSubscriber();

        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, subscriber.getId(), organizationId);

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            UserOrganization userOrgEntity = userOrganizationRepository
                    .findByWorkspaceIdAndUserId(organizationId, userRoleRest.getUserId()).orElseThrow();

            // delete linked roles from table g4it_user_role_organization if exist
            userRoleOrganizationRepository.deleteByUserOrganizations(userOrgEntity);

            // delete user-organization link
            userOrganizationRepository.deleteById(userOrgEntity.getId());

            userService.clearUserCache(userRestMapper.toBusinessObject(userOrgEntity.getUser()), subscriber.getName(), organizationId);
        }
    }

}
