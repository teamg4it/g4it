/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.model.RoleBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AdministratorRoleService {

    RoleService roleService;

    /**
     * Get list of all the Roles from g4it_role
     *
     * @param user the current user.
     */
    public List<RoleBO> getAllRoles(final UserBO user) {
        hasAdminRightsOnAnyOrganizationOrAnyWorkspace(user);
        return roleService.getAllRolesBO();
    }

    public void hasAdminRightsOnAnyOrganizationOrAnyWorkspace(UserBO user) {
        if (!(roleService.hasAdminRightsOnAnyOrganization(user) || roleService.hasAdminRightsOnAnyWorkspace(user))) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, String.format("User with id '%d' do not have admin role", user.getId()));
        }
    }

    public void hasAdminRightOnOrganizationOrWorkspace(UserBO user, Long organizationId, Long workspaceId) {
        if (!(roleService.hasAdminRightsOnOrganization(user, organizationId) || roleService.hasAdminRightsOnWorkspace(user, workspaceId))) {
            throw new AuthorizationException(
                    HttpServletResponse.SC_FORBIDDEN,
                    String.format("User with id '%d' do not have admin role on organization '%d' or workspace '%d'", user.getId(), organizationId, workspaceId));
        }
    }

    public void hasAdminRightsOnAnyOrganization(UserBO user) {
        if (!roleService.hasAdminRightsOnAnyOrganization(user)) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, String.format("User with id '%d' do not have admin role on any organization", user.getId()));
        }
    }

    /**
     * @param user                       user BO
     * @param organizationId             organization's id
     * @param hasOrganizationAdminRights has organization admin role
     * @param hasDomainAuthorization     has valid domain
     */
    public void hasOrganizationAdminOrDomainAccess(UserBO user, Long organizationId,
                                                   boolean hasOrganizationAdminRights,
                                                   boolean hasDomainAuthorization) {
        if (!(hasOrganizationAdminRights || hasDomainAuthorization)) {
            throw new AuthorizationException(
                    HttpServletResponse.SC_FORBIDDEN,
                    String.format("User with id '%d' has no admin role on organization '%d' or has domain not authorized.",
                            user.getId(), organizationId)
            );
        }
    }

    /**
     * Check if user has workspace admin access and ROLE_ECO_MIND_AI_WRITE role on a specific workspace
     *
     * @param user        the user BO
     * @param workspaceId the workspace id
     */
    public void hasWorkspaceAdminAndEcoMindAIWriteRole(UserBO user, Long workspaceId) {
        if(Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) {
            return; // Super admin has all rights
        }
        boolean hasWorkspaceAdmin = roleService.hasWorkspaceAdminRights(user, workspaceId);
        boolean hasEcoMindAIWriteRole = user.getOrganizations().stream()
                .anyMatch(orgBO -> orgBO.getWorkspaces().stream()
                        .filter(workspaceBO -> workspaceBO.getId().equals(workspaceId))
                        .anyMatch(workspaceBO -> workspaceBO.getRoles().contains(com.soprasteria.g4it.backend.common.utils.Constants.ROLE_ECO_MIND_AI_WRITE))
                );

        if (!(hasWorkspaceAdmin && hasEcoMindAIWriteRole)) {
            throw new AuthorizationException(
                    HttpServletResponse.SC_FORBIDDEN,
                    String.format("User with id '%d' does not have workspace admin access or ROLE_ECO_MIND_AI_WRITE role on workspace '%d'",
                            user.getId(), workspaceId)
            );
        }
    }

}
