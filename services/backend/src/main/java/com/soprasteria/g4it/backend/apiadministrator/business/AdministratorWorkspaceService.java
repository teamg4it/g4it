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
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserInfoBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpsertRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * Administrator Workspace service.
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
    UserWorkspaceRepository userWorkspaceRepository;
    @Autowired
    UserOrganizationRepository userOrganizationRepository;
    @Autowired
    UserRoleWorkspaceRepository userRoleWorkspaceRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRestMapper userRestMapper;
    @Autowired
    UserService userService;
    @Autowired
    AuthService authService;

    /**
     * Get the list of active workspaces with admin role attached to organization
     * to be displayed in 'manage users' screen.
     * filter using organization or workspace or both.
     *
     * @param organizationId the client organization id.
     * @param workspaceId    the workspace id.
     * @param user           the user.
     * @return list of organizationBO.
     */
    public List<OrganizationBO> getWorkspaces(final Long organizationId, final Long workspaceId, final UserBO user) {

        if (workspaceId == null && organizationId == null) {
            administratorRoleService.hasAdminRightsOnAnyOrganizationOrAnyWorkspace(user);
        }

        return user.getOrganizations().stream()
                .filter(organizationBO -> organizationId == null || Objects.equals(organizationBO.getId(), organizationId))
                .peek(orgBO -> {
                    final var workspaces = orgBO.getWorkspaces().stream()
                            .filter(workspaceBO -> workspaceId == null || Objects.equals(workspaceBO.getId(), workspaceId))
                            .filter(workspaceBO -> Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS.contains(workspaceBO.getStatus()))
                            .toList();
                    orgBO.setWorkspaces(workspaces);
                })
                .toList();

    }

    /**
     * Update the workspace.
     *
     * @param workspaceUpsertRest the workspaceUpsertRest.
     * @param user                   the user.
     * @return WorkspaceBO
     */
    public WorkspaceBO updateWorkspace(final Long workspaceId, final WorkspaceUpsertRest workspaceUpsertRest, UserBO user) {
        // Check Admin Role on this organization or workspace.
        administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(user, workspaceUpsertRest.getOrganizationId(), workspaceId);
        WorkspaceBO workspaceBO = workspaceService.updateWorkspace(workspaceId, workspaceUpsertRest, user.getId());
        userService.clearUserAllCache();
        return workspaceBO;
    }

    /**
     * Create a workspace.
     *
     * @param workspaceUpsertRest the workspaceUpsertRest.
     * @param user                the user.
     * @return WorkspaceBO
     */
    public WorkspaceBO createWorkspace(WorkspaceUpsertRest workspaceUpsertRest, UserBO user, boolean checkAdminRole) {
        Long organizationId = workspaceUpsertRest.getOrganizationId();
        boolean hasOrganizationAdminRights = roleService.hasAdminRightsOnOrganization(user, organizationId);
        boolean hasDomainAuthorization = roleService.isUserDomainAuthorized(user, organizationId);

        // Check Admin Role on this organization or the logged-in workspace admin user's domain is authorized
        if (checkAdminRole) {
            administratorRoleService.hasOrganizationAdminOrDomainAccess(user, organizationId, hasOrganizationAdminRights,
                    hasDomainAuthorization);
        }

        final WorkspaceBO result = workspaceService.createWorkspace(workspaceUpsertRest, user, organizationId);
        userService.clearUserCache(user);

        if (hasOrganizationAdminRights)
            return result;

        // Link user to the Workspace and assign with workspace admin role
        if (!(checkAdminRole) || hasDomainAuthorization) {
            UserRoleRest userRoleRest = UserRoleRest.builder().userId(user.getId()).roles(List.of("ROLE_ORGANIZATION_ADMINISTRATOR")).build();
            LinkUserRoleRest linkUserRoleRest = LinkUserRoleRest.builder().workspaceId(result.getId()).users(Collections.singletonList(userRoleRest)).build();
            linkUserToWorkspace(linkUserRoleRest, authService.getUser(), false);
        }

        return result;
    }

    /**
     * Get list of all the users linked to a workspace
     *
     * @param workspaceId the workspace id.
     * @param user        the current user
     */
    public List<UserInfoBO> getUsersOfWorkspace(Long workspaceId, final UserBO user) {
        final Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new G4itRestException("404", String.format("Workspace %d not found.", workspaceId)));
        long organizationId = workspace.getOrganization().getId();
        administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(user, organizationId, workspaceId);

        // fetch organization admins
        List<UserInfoBO> organizationAdmins = new ArrayList<>(userOrganizationRepository.findByOrganization(workspace.getOrganization())).stream()
                .<UserInfoBO>map(userOrganization -> {
                    List<Role> roles = userOrganization.getRoles();
                    if (roles.stream().noneMatch(role -> role.getName().equals(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR))) {
                        return null;
                    }

                    User u = userOrganization.getUser();
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

        List<Long> orgAdminIds = organizationAdmins.stream()
                .map(UserInfoBO::getId)
                .toList();

        List<UserInfoBO> usersByWorkspace = userWorkspaceRepository.findByWorkspace(workspace).stream()
                .filter(userWorkspace -> !orgAdminIds.contains(userWorkspace.getUser().getId()))
                .filter(userWorkspace -> !Constants.SUPER_ADMIN_EMAIL.equalsIgnoreCase(userWorkspace.getUser().getEmail()))
                .<UserInfoBO>map(userWorkspace -> {
                    User u = userWorkspace.getUser();
                    return UserInfoBO.builder()
                            .id(u.getId())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .email(u.getEmail())
                            .roles(userWorkspace.getRoles().stream().map(Role::getName).toList())
                            .build();
                })
                .toList();

        // Retrieve all users, including organization admins, when the logged-in user is an organization admin
        if (roleService.hasAdminRightsOnOrganization(user, organizationId)) {
            return Stream.concat(organizationAdmins.stream(), usersByWorkspace.stream()).toList();
        }
        //  Return the users except for organization admins when the logged-in user is a workspace admin.
        else {
            return usersByWorkspace;
        }
    }


    /**
     * link user to a workspace
     *
     * @param linkUserRoleRest the linkUserRoleRest
     * @param user             the user
     */
    public List<UserInfoBO> linkUserToWorkspace(final LinkUserRoleRest linkUserRoleRest, final UserBO user, boolean checkAdminRole) {

        Long workspaceId = linkUserRoleRest.getWorkspaceId();

        final Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new G4itRestException("404", String.format("WorkspaceId %s is not found", workspaceId)));

        if (checkAdminRole) {
            administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(user, workspace.getOrganization().getId(), workspaceId);
        }
        List<UserInfoBO> userInfoList = new ArrayList<>();

        List<Role> allRoles = roleService.getAllRoles();

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            User userEntity = userRepository.findById(userRoleRest.getUserId()).orElseThrow();

            Optional<UserWorkspace> userWorkOptional = userWorkspaceRepository.findByWorkspaceIdAndUserId(workspaceId, userRoleRest.getUserId());

            UserWorkspace userWorkspace;

            if (userWorkOptional.isEmpty()) {
                userWorkspace = UserWorkspace.builder().
                        workspace(workspace)
                        .user(userEntity)
                        .defaultFlag(true)
                        .build();

                userWorkspaceRepository.save(userWorkspace);
            } else {
                userWorkspace = userWorkOptional.get();

                // delete linked roles from table g4it_user_role_organization if exist
                userRoleWorkspaceRepository.deleteByUserWorkspaces(userWorkspace);
            }

            final List<Role> userRolesToAdd = userRoleRest.getRoles() == null ?
                    List.of() :
                    allRoles.stream()
                            .filter(role -> userRoleRest.getRoles().contains(role.getName()))
                            .toList();

            List<UserRoleWorkspace> userRoleWorkspaces = userRolesToAdd.stream()
                    .<UserRoleWorkspace>map(role ->
                            UserRoleWorkspace.builder()
                                    .userWorkspaces(userWorkspace)
                                    .roles(role)
                                    .build()
                    )
                    .toList();

            userRoleWorkspaceRepository.saveAll(userRoleWorkspaces);

            // Create and add UserInfoBO to the list
            userInfoList.add(UserInfoBO.builder()
                    .id(userRoleRest.getUserId())
                    .firstName(userEntity.getFirstName())
                    .lastName(userEntity.getLastName())
                    .email(userEntity.getEmail())
                    .roles(userRolesToAdd.stream().map(Role::getName).toList())
                    .build());


            userService.clearUserCache(userRestMapper.toBusinessObject(userEntity), workspace.getOrganization().getName(), workspaceId);
        }

        return userInfoList;
    }

    /**
     * Delete user-workspace link
     *
     * @param linkUserRoleRest the linkUserRoleRest
     * @param user             the  user
     */
    public void deleteUserWorkLink(final LinkUserRoleRest linkUserRoleRest, final UserBO user) {

        Long workspaceId = linkUserRoleRest.getWorkspaceId();
        Organization organization = workspaceRepository.findById(workspaceId).orElseThrow().getOrganization();

        administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(user, organization.getId(), workspaceId);

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            UserWorkspace userOrgEntity = userWorkspaceRepository
                    .findByWorkspaceIdAndUserId(workspaceId, userRoleRest.getUserId()).orElseThrow();

            // delete linked roles from table g4it_user_role_organization if exist
            userRoleWorkspaceRepository.deleteByUserWorkspaces(userOrgEntity);

            // delete user-workspace link
            userWorkspaceRepository.deleteById(userOrgEntity.getId());

            userService.clearUserCache(userRestMapper.toBusinessObject(userOrgEntity.getUser()), organization.getName(), workspaceId);
        }
    }

}
