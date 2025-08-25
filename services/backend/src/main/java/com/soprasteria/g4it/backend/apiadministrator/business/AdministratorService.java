/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;


import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserSearchBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.CriteriaRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Organization service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AdministratorService {

    /**
     * Repository to access subscriber data.
     */
    @Autowired
    OrganizationRepository organizationRepository;
    /**
     * Repository to access user data.
     */
    UserRepository userRepository;
    /**
     * Subscriber Mapper.
     */
    @Autowired
    OrganizationRestMapper organizationRestMapper;
    /**
     * The Administrator Role Service
     */
    @Autowired
    private AdministratorRoleService administratorRoleService;
    /**
     * The Subscriber Service
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * The User Service
     */
    @Autowired
    private UserService userService;

    /**
     * Retrieve the list of subscribers for the user which has ROLE_SUBSCRIBER_ADMINISTRATOR on it
     *
     * @param user the user.
     * @return the List<SubscriberBO>.
     */
    public List<OrganizationBO> getOrganizations(final UserBO user) {
        administratorRoleService.hasAdminRightsOnAnySubscriberOrAnyOrganization(user);

        List<OrganizationBO> resultSubscriberAdmin = user.getSubscribers().stream()
                .filter(subscriberBO -> subscriberBO.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR)).toList();

        List<OrganizationBO> checkOrgAdmin = user.getSubscribers().stream()
                .filter(subscriberBO -> subscriberBO.getRoles().isEmpty()).toList();
        List<OrganizationBO> result = new ArrayList<>(resultSubscriberAdmin);
        for (OrganizationBO subBO : checkOrgAdmin) {
            List<WorkspaceBO> organization = subBO.getOrganizations().stream().filter(organizationBO -> organizationBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)).toList();
            if (!organization.isEmpty()) {
                subBO.setOrganizations(organization);
                result.add(subBO);
            }
        }
        return result;
    }

    /**
     * @param subscriberId the subscriber id
     * @param criteriaRest criteria to set
     * @param user         the current user
     * @return the SubscriberBo with updated criteria
     */
    public OrganizationBO updateOrganizationCriteria(final Long subscriberId, final CriteriaRest criteriaRest, final UserBO user) {
        administratorRoleService.hasAdminRightsOnAnySubscriber(user);
        Organization organizationToUpdate = organizationService.getSubscriptionById(subscriberId);
        organizationToUpdate.setCriteria(criteriaRest.getCriteria());
        organizationRepository.save(organizationToUpdate);
        userService.clearUserAllCache();
        return organizationRestMapper.toBusinessObject(organizationToUpdate);
    }

    /**
     * Get all the users (filtered by authorized_domains of subscriber)
     *
     * @param searchedName the string to be searched
     * @param subscriberId the subscriber's id
     * @param user         the  user
     */
    public List<UserSearchBO> searchUserByName(final String searchedName,
                                               final Long subscriberId,
                                               final Long organizationId,
                                               final UserBO user) {

        administratorRoleService.hasAdminRightOnSubscriberOrOrganization(user, subscriberId, organizationId);

        Organization organization = organizationRepository.findById(subscriberId)
                .orElseThrow(() -> new G4itRestException("404", String.format("Subscriber %d not found.", subscriberId)));

        if (organization.getAuthorizedDomains() == null) return List.of();

        Set<String> domains = Arrays.stream(organization.getAuthorizedDomains().replaceAll("\\s+", "").split(","))
                .collect(Collectors.toSet());

        final List<User> searchedList = new ArrayList<>();
        if (searchedName.contains("@")) {
            userRepository.findByEmail(searchedName).ifPresent(searchedList::add);
        }

        if (searchedList.isEmpty()) {
            searchedList.addAll(userRepository.findBySearchedName(searchedName, domains));
        }

        if (searchedList.isEmpty()) return List.of();

        return searchedList.stream()
                .<UserSearchBO>map(searchedUser -> {

                    List<String> userRoles = new ArrayList<>();
                    if (searchedUser.getUserWorkspaces() != null) {
                        userRoles.addAll(searchedUser.getUserWorkspaces().stream().filter(org -> org.getWorkspace().getId() == organizationId)
                                .findFirst()
                                .orElse(UserWorkspace.builder().roles(List.of()).build())
                                .getRoles().stream().map(Role::getName).toList());
                    }

                    if (searchedUser.getUserOrganizations() != null) {
                        userRoles.addAll(searchedUser.getUserOrganizations().stream()
                                .filter(userSubscriber -> userSubscriber.getOrganization().getId() == subscriberId)
                                .map(userSubscriber -> userSubscriber.getUserRoleOrganization().stream()
                                        .map(userRoleSubscriber -> userRoleSubscriber.getRoles().getName())
                                        .toList()
                                )
                                .flatMap(Collection::stream)
                                .toList());
                    }

                    List<Long> linkedOrgIds = searchedUser.getUserWorkspaces() == null ? List.of() :
                            searchedUser.getUserWorkspaces().stream()
                                    .map(userOrg -> userOrg.getWorkspace().getId())
                                    .toList();

                    return UserSearchBO.builder()
                            .id(searchedUser.getId())
                            .firstName(searchedUser.getFirstName())
                            .lastName(searchedUser.getLastName())
                            .email(searchedUser.getEmail())
                            .linkedOrgIds(linkedOrgIds)
                            .roles(userRoles)
                            .build();
                })
                .toList();
    }

    public OrganizationBO getSubscriberById(Long id) {
        return organizationRestMapper.toBusinessObject(organizationRepository.findById(id).orElse(null));
    }

}
