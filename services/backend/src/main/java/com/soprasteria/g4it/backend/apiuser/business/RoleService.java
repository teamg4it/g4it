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

import java.util.List;
import java.util.Objects;

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
     * Validate if user have 'SUBSCRIBER_ADMINISTRATOR' role on any subscriber.
     *
     * @param user the user.
     * @return boolean
     */
    public boolean hasAdminRightsOnAnySubscriber(final UserBO user) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getSubscribers().stream()
                .anyMatch(subscriberBO -> subscriberBO.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR));
    }

    /**
     * Check if user have 'ORGANIZATION_ADMINISTRATOR' role on any organization.
     *
     * @param user the user.
     */
    public boolean hasAdminRightsOnAnyOrganization(final UserBO user) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getSubscribers().stream()
                .anyMatch(subscriberBO -> subscriberBO.getOrganizations().stream()
                        .anyMatch(organizationBO -> organizationBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)));
    }


    /**
     * Validate if user have 'SUBSCRIBER_ADMINISTRATOR' role on subscriber.
     *
     * @param user         the user.
     * @param subscriberId the subscriber's id.
     * @return boolean
     */
    public boolean hasAdminRightsOnSubscriber(final UserBO user, final Long subscriberId) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getSubscribers().stream()
                .filter(subscriberBO -> Objects.equals(subscriberBO.getId(), subscriberId))
                .anyMatch(subscriberBO -> subscriberBO.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR));
    }

    /**
     * Check if user has 'ORGANIZATION_ADMINISTRATOR' the organization id.
     *
     * @param user           the user.
     * @param organizationId the organization's id
     * @return boolean
     */
    public boolean hasAdminRightsOnOrganization(UserBO user, Long organizationId) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return user.getSubscribers().stream()
                .anyMatch(subscriberBO -> subscriberBO.getOrganizations().stream()
                        .filter(organizationBO -> Objects.equals(organizationBO.getId(), organizationId))
                        .anyMatch(organizationBO -> organizationBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)));
    }

    public List<RoleBO> getAllRolesBO() {
        return roleMapper.toDto(roleRepository.findAll());
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public boolean hasAdminRightOnSubscriberOrOrganization(UserBO user, Long subscriberId, Long organizationId) {
        if (Constants.SUPER_ADMIN_EMAIL.equals(user.getEmail())) return true;
        return this.hasAdminRightsOnSubscriber(user, subscriberId) || this.hasAdminRightsOnOrganization(user, organizationId);
    }
}
