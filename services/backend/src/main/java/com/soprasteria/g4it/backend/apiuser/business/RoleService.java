/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.mapper.RoleMapper;
import com.soprasteria.g4it.backend.apiuser.model.RoleBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.repository.RoleRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class RoleService {

    /**
     * Repository to access role data.
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Mapper for role data.
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * Validate if user have 'SUBSCRIBER_ADMINISTRATOR' role on any organization.
     *
     * @param user the user.
     * @return boolean
     */
    public boolean hasAdminRightsOnAnyOrganization(final UserBO user) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getOrganizations().stream()
                .anyMatch(organizationBO -> organizationBO.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR));
    }

    /**
     * Check if user have 'ORGANIZATION_ADMINISTRATOR' role on any organization.
     *
     * @param user the user.
     */
    public boolean hasAdminRightsOnAnyWorkspace(final UserBO user) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getOrganizations().stream()
                .anyMatch(orgBO -> orgBO.getWorkspaces().stream()
                        .anyMatch(workspaceBO -> workspaceBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)));
    }


    /**
     * Validate if user have 'SUBSCRIBER_ADMINISTRATOR' role on organization.
     *
     * @param user           the user.
     * @param organizationId the organization's id.
     * @return boolean
     */
    public boolean hasAdminRightsOnOrganization(final UserBO user, final Long organizationId) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getOrganizations().stream()
                .filter(orgBO -> Objects.equals(orgBO.getId(), organizationId))
                .anyMatch(orgBO -> orgBO.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR));
    }

    /**
     * Check if user has 'ORGANIZATION_ADMINISTRATOR' the organization id.
     *
     * @param user           the user.
     * @param organizationId the organization's id
     * @return boolean
     */
    public boolean hasAdminRightsOnWorkspace(UserBO user, Long organizationId) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getOrganizations().stream()
                .anyMatch(orgBO -> orgBO.getWorkspaces().stream()
                        .filter(workspaceBO -> Objects.equals(workspaceBO.getId(), organizationId))
                        .anyMatch(workspaceBO -> workspaceBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)));
    }

    /**
     * Check if logged in 'ORGANIZATION_ADMINISTRATOR' user's domain
     * belongs to the organization's authorized domain
     *
     * @param user           the user.
     * @param organizationId the organization's id
     * @return boolean
     */
    public boolean isUserDomainAuthorized(UserBO user, Long organizationId) {
        String userDomain = user.getEmail().split("@")[1];
        return user.getOrganizations().stream()
                .filter(orgBO -> Objects.equals(orgBO.getId(), organizationId))
                .anyMatch(orgBO ->
                        Arrays.stream(
                                        Optional.ofNullable(orgBO.getAuthorizedDomains())
                                                .orElse("")
                                                .split(",")
                                )
                                .map(String::trim)
                                .anyMatch(domain -> domain.equals(userDomain))
                );
    }


    public List<RoleBO> getAllRolesBO() {
        return roleMapper.toDto(roleRepository.findAll());
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public boolean hasAdminRightOnOrganizationOrWorkspace(UserBO user, Long organizationId, Long workspaceId) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return this.hasAdminRightsOnOrganization(user, organizationId) || this.hasAdminRightsOnWorkspace(user, workspaceId);
    }
}
