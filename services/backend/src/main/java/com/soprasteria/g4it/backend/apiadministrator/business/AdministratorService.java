/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;


import com.soprasteria.g4it.backend.apiuser.business.SubscriberService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.UserSearchBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
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
    SubscriberRepository subscriberRepository;
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
    private SubscriberService subscriberService;

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
    public List<SubscriberBO> getSubscribers(final UserBO user) {
        administratorRoleService.hasAdminRightsOnAnySubscriberOrAnyOrganization(user);

        List<SubscriberBO> resultSubscriberAdmin = user.getSubscribers().stream()
                .filter(subscriberBO -> subscriberBO.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR)).toList();

        List<SubscriberBO> checkOrgAdmin = user.getSubscribers().stream()
                .filter(subscriberBO -> subscriberBO.getRoles().isEmpty()).toList();
        List<SubscriberBO> result = new ArrayList<>(resultSubscriberAdmin);
        for (SubscriberBO subBO : checkOrgAdmin) {
            List<OrganizationBO> organization = subBO.getOrganizations().stream().filter(organizationBO -> organizationBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)).toList();
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
    public SubscriberBO updateSubscriberCriteria(final Long subscriberId, final CriteriaRest criteriaRest, final UserBO user) {
        administratorRoleService.hasAdminRightsOnAnySubscriber(user);
        Subscriber subscriberToUpdate = subscriberService.getSubscriptionById(subscriberId);
        subscriberToUpdate.setCriteria(criteriaRest.getCriteria());
        subscriberRepository.save(subscriberToUpdate);
        userService.clearUserAllCache();
        return organizationRestMapper.toBusinessObject(subscriberToUpdate);
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

        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new G4itRestException("404", String.format("Subscriber %d not found.", subscriberId)));

        if (subscriber.getAuthorizedDomains() == null) return List.of();

        Set<String> domains = Arrays.stream(subscriber.getAuthorizedDomains().replaceAll("\\s+", "").split(","))
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
                    if (searchedUser.getUserOrganizations() != null) {
                        userRoles.addAll(searchedUser.getUserOrganizations().stream().filter(org -> org.getWorkspace().getId() == organizationId)
                                .findFirst()
                                .orElse(UserOrganization.builder().roles(List.of()).build())
                                .getRoles().stream().map(Role::getName).toList());
                    }

                    if (searchedUser.getUserSubscribers() != null) {
                        userRoles.addAll(searchedUser.getUserSubscribers().stream()
                                .filter(userSubscriber -> userSubscriber.getSubscriber().getId() == subscriberId)
                                .map(userSubscriber -> userSubscriber.getUserRoleSubscriber().stream()
                                        .map(userRoleSubscriber -> userRoleSubscriber.getRoles().getName())
                                        .toList()
                                )
                                .flatMap(Collection::stream)
                                .toList());
                    }

                    List<Long> linkedOrgIds = searchedUser.getUserOrganizations() == null ? List.of() :
                            searchedUser.getUserOrganizations().stream()
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

    public SubscriberBO getSubscriberById(Long id) {
        return organizationRestMapper.toBusinessObject(subscriberRepository.findById(id).orElse(null));
    }

}
