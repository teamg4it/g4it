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
        hasAdminRightsOnAnySubscriberOrAnyOrganization(user);
        return roleService.getAllRolesBO();
    }

    public void hasAdminRightsOnAnySubscriberOrAnyOrganization(UserBO user) {
        if (!(roleService.hasAdminRightsOnAnySubscriber(user) || roleService.hasAdminRightsOnAnyOrganization(user))) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, String.format("User with id '%d' do not have admin role", user.getId()));
        }
    }

    public void hasAdminRightOnSubscriberOrOrganization(UserBO user, Long subscriberId, Long organizationId) {
        if (!(roleService.hasAdminRightsOnSubscriber(user, subscriberId) || roleService.hasAdminRightsOnOrganization(user, organizationId))) {
            throw new AuthorizationException(
                    HttpServletResponse.SC_FORBIDDEN,
                    String.format("User with id '%d' do not have admin role on subscriber '%d' or organization '%d'", user.getId(), subscriberId, organizationId));
        }
    }

    public void hasAdminRightsOnAnySubscriber(UserBO user) {
        if (!roleService.hasAdminRightsOnAnySubscriber(user)) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, String.format("User with id '%d' do not have admin role on any subscriber", user.getId()));
        }
    }

    public void hasAdminRightsOnSubscriber(UserBO user, Long subscriberId, Boolean isOrgAdminAuthorized) {
        if (!(roleService.hasAdminRightsOnSubscriber(user, subscriberId) || isOrgAdminAuthorized)) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, String.format("User with id '%d' do not have admin role on subscriber with id :" +
                    " '%d' or doesn't have authorized domain", user.getId(), subscriberId));
        }
    }

}
