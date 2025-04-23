/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Service
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriberRepository subscriberRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NewUserService newUserService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private UserSubscriberRepository userSubscriberRepository;

    private static final String USER = "getUserByName";
    private static final String TOKEN = "getJwtToken";

    /**
     * Create a UserBo object from jwt token
     *
     * @param jwt the token
     * @return the userBo
     */
    public UserBO getUserFromToken(final Jwt jwt) {
        final String email = jwt.getClaim(Constants.JWT_EMAIL_FIELD);
        return UserBO.builder()
                .email(email)
                .firstName(jwt.getClaim(Constants.JWT_FIRST_NAME))
                .lastName(jwt.getClaim(Constants.JWT_LAST_NAME))
                .sub(jwt.getClaim(Constants.JWT_SUB))
                .domain(email.split("@")[1])
                .build();
    }

    /**
     * Gets user's information by its name.
     *
     * @param userInfo the userInfo (email, firstName, lastname and subject)
     * @return the user rest object.
     */
    @Transactional
    @Cacheable("getUserByName")
    public UserBO getUserByName(final UserBO userInfo) {
        String sub = userInfo.getSub();
        String email = userInfo.getEmail();
        Optional<User> userSubject = userRepository.findBySub(sub);
        User userReturned = null;
        if (userSubject.isPresent()) {
            // sub present with diff email address: user info in g4it_user with new values
            userReturned = userSubject.get();
            if (!email.equals(userReturned.getEmail()) || !userInfo.getLastName().equals(userReturned.getLastName())) {
                updateUserWithNewUserinfo(userReturned, userInfo);
            }
        }

        if (userReturned == null) {
            Optional<User> userOptional = userRepository.findByEmail(email);

            // sub not present but has email address: user info in g4it_user with new values
            userReturned = userOptional
                    .map(user -> updateUserWithNewUserinfo(user, userInfo))
                    .orElseGet(() -> createUser(userInfo));
        }

        if (userReturned == null) {
            return null;
        }

        List<SubscriberBO> subscriberBOList = Constants.SUPER_ADMIN_EMAIL.equals(email) ?
                buildSubscribersForSuperAdmin() :
                buildSubscribers(userReturned, userInfo.isAdminMode());

        return UserBO.builder()
                .id(userReturned.getId())
                .firstName(userReturned.getFirstName())
                .lastName(userReturned.getLastName())
                .email(userReturned.getEmail())
                .isSuperAdmin(Constants.SUPER_ADMIN_EMAIL.equals(userReturned.getEmail()))
                .subscribers(subscriberBOList)
                .adminMode(userInfo.isAdminMode())
                .build();
    }

    /**
     * Create the user depending on its email
     *
     * @param userInfo the userinfo
     * @return the user created
     */
    private User createUser(final UserBO userInfo) {
        User userReturned = null;
        if (Constants.SUPER_ADMIN_EMAIL.equals(userInfo.getEmail())) {
            User newUser = createNewUserWithDomain(null, userInfo);
            if (newUser != null) {
                userReturned = userRepository.findById(newUser.getId()).orElseThrow();

            }
        } else {
            List<Role> accessRoles = List.of(
                    roleRepository.findByName(Constants.ROLE_INVENTORY_READ),
                    roleRepository.findByName(Constants.ROLE_DIGITAL_SERVICE_READ),
                    roleRepository.findByName(Constants.ROLE_DIGITAL_SERVICE_WRITE)
            );

            User newUser = createNewUserWithDomain(accessRoles, userInfo);

            if (newUser == null) {
                // user's domain doesn't exist in g4it_subscriber authorized domain, add new user to g4it_user with no rights
                newUserService.createNewUser(userInfo);
            } else
                userReturned = userRepository.findById(newUser.getId()).orElseThrow();
        }
        return userReturned;
    }

    /**
     * Create new user with authorized domains
     *
     * @param accessRoles the default access roles
     * @param userInfo    the user info
     * @return the new user created
     */
    private User createNewUserWithDomain(List<Role> accessRoles, UserBO userInfo) {
        User newUser = null;
        for (Subscriber subscriber : subscriberRepository.findByAuthorizedDomainsNotNull()) {
            if (Arrays.asList(subscriber.getAuthorizedDomains().split(",")).contains(userInfo.getDomain())) {
                Organization demoOrg = organizationRepository.findBySubscriberNameAndName(subscriber.getName(), Constants.DEMO)
                        .orElseGet(() -> organizationRepository.save(Organization.builder()
                                .name(Constants.DEMO)
                                .status(OrganizationStatus.ACTIVE.name())
                                .creationDate(LocalDateTime.now())
                                .subscriber(subscriber)
                                .build()));
                newUser = newUserService.createUser(subscriber, demoOrg, newUser, userInfo, accessRoles);
                clearUserCache(userInfo);
            }
        }
        return newUser;
    }

    /**
     * update user.
     *
     * @param user     the user.
     * @param userInfo the user's information
     * @return the user with updated user info.
     */
    private User updateUserWithNewUserinfo(final User user, final UserBO userInfo) {
        user.setSub(userInfo.getSub());
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setDomain(userInfo.getDomain());
        userRepository.save(user);
        return user;
    }

    /**
     * Create an admin user in 'nosecurity' mode
     *
     * @param withSubscribers include subscribers
     * @return the userBO
     */
    @Transactional
    public UserBO getNoSecurityUser(boolean withSubscribers) {
        User user = userRepository.findByEmail(Constants.SUPER_ADMIN_EMAIL).orElseThrow();

        return UserBO.builder()
                .id(user.getId())
                .firstName("Admin")
                .lastName("No Security Mode")
                .email(Constants.SUPER_ADMIN_EMAIL)
                .isSuperAdmin(true)
                .subscribers(withSubscribers ? buildSubscribersForSuperAdmin() : null)
                .adminMode(true)
                .domain(Constants.SUPER_ADMIN_EMAIL.split("@")[1])
                .build();
    }

    /**
     * Build subscriber list.
     *
     * @return the user's subscriber list.
     */
    public List<SubscriberBO> buildSubscribersForSuperAdmin() {

        // Get the subscribers and subObjects on which the user has ROLE_SUBSCRIBER_ADMINISTRATOR
        return subscriberRepository.findAll().stream()
                .map(subscriber -> {
                    var subscriberBO = SubscriberBO.builder()
                            .defaultFlag(false)
                            .name(subscriber.getName())
                            .organizations(subscriber.getOrganizations().stream()
                                    .map(organization -> {
                                        OrganizationBO organizationBO = OrganizationBO.builder()
                                                .roles(List.of())
                                                .defaultFlag(false)
                                                .name(organization.getName())
                                                .id(organization.getId())
                                                .status(organization.getStatus())
                                                .deletionDate(organization.getDeletionDate())
                                                .criteriaIs(organization.getCriteriaIs())
                                                .criteriaDs(organization.getCriteriaDs())
                                                .build();
                                        return organizationBO;
                                    })
                                    .sorted(Comparator.comparing(OrganizationBO::getName))
                                    .toList())
                            .roles(List.of(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR))
                            .criteria(subscriber.getCriteria())
                            .authorizedDomains(subscriber.getAuthorizedDomains())
                            .id(subscriber.getId())
                            .build();
                    return subscriberBO;
                })
                .sorted(Comparator.comparing(SubscriberBO::getName))
                .toList();
    }

    /**
     * Build subscriber list.
     *
     * @param user the user.
     * @return the user's subscriber list.
     */
    public List<SubscriberBO> buildSubscribers(final User user, final boolean adminMode) {

        if (user.getUserSubscribers() == null || user.getUserOrganizations() == null) return List.of();

        // Get the subscribers and subObjects on which the user has ROLE_SUBSCRIBER_ADMINISTRATOR
        List<SubscriberBO> results = new ArrayList<>(user.getUserSubscribers().stream()
                .filter(userSubscriber -> userSubscriber.getRoles() != null &&
                        userSubscriber.getRoles().stream().anyMatch(role -> role.getName().equals(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR)))
                .map(userSubscriber -> buildSubscriber(userSubscriber, adminMode))
                .sorted(Comparator.comparing(SubscriberBO::getName))
                .toList());

        Set<String> adminSubscribers = results.stream().map(SubscriberBO::getName).collect(Collectors.toSet());

        if (user.getUserOrganizations() == null) return results;

        // (subscriber, [userOrganization])
        final Map<Subscriber, List<UserOrganization>> organizationBySubscriber = user.getUserOrganizations().stream()
                .collect(Collectors.groupingBy(e -> e.getOrganization().getSubscriber()));

        // Get the subscribers and subObjects on which the user has not ROLE_SUBSCRIBER_ADMINISTRATOR but other roles on organizations
        results.addAll(organizationBySubscriber.entrySet().stream()
                .filter(entry -> !adminSubscribers.contains(entry.getKey().getName()))
                .map(userOrgsBySub -> buildSubscriberWithUserOrganizations(userOrgsBySub.getKey(), userOrgsBySub.getValue(), adminMode))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SubscriberBO::getName))
                .toList());

        return results;
    }

    /**
     * Build subscriber.
     *
     * @param userSubscriber the user subscriber.
     * @param adminMode      the adminMode
     * @return the user's subscriber.
     */
    public SubscriberBO buildSubscriber(final UserSubscriber userSubscriber, final boolean adminMode) {

        final List<Role> roles = userSubscriber.getRoles() == null ? List.of() : userSubscriber.getRoles();
        final List<String> status = adminMode ? Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS : List.of(OrganizationStatus.ACTIVE.name());

        return SubscriberBO.builder()
                .defaultFlag(userSubscriber.getDefaultFlag())
                .name(userSubscriber.getSubscriber().getName())
                .organizations(userSubscriber.getSubscriber().getOrganizations().stream()
                        .filter(organization -> status.contains(organization.getStatus()))
                        .<OrganizationBO>map(organization ->
                                OrganizationBO.builder()
                                        .roles(List.of())
                                        .defaultFlag(false)
                                        .name(organization.getName())
                                        .id(organization.getId())
                                        .status(organization.getStatus())
                                        .deletionDate(organization.getDeletionDate())
                                        .criteriaIs(organization.getCriteriaIs())
                                        .criteriaDs(organization.getCriteriaDs())
                                        .build()
                        )
                        .sorted(Comparator.comparing(OrganizationBO::getName))
                        .toList())
                .roles(roles.stream().map(Role::getName).toList())
                .criteria(userSubscriber.getSubscriber().getCriteria())
                .authorizedDomains(userSubscriber.getSubscriber().getAuthorizedDomains())
                .id(userSubscriber.getSubscriber().getId())
                .build();
    }

    /**
     * Build the subscriber if any organization has at least one user role
     *
     * @param subscriber        the user subscriber.
     * @param userOrganizations the user's organization list.
     * @param adminMode         the adminMode
     * @return the user's subscriber.
     */
    public SubscriberBO buildSubscriberWithUserOrganizations(final Subscriber subscriber, final List<UserOrganization> userOrganizations, final boolean adminMode) {

        final List<String> status = adminMode ? Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS : List.of(OrganizationStatus.ACTIVE.name());

        List<OrganizationBO> organizations = userOrganizations.stream()
                .filter(userOrganization -> status.contains(userOrganization.getOrganization().getStatus()))
                .map(this::buildOrganization)
                .filter(Objects::nonNull)
                .toList();

        if (organizations.isEmpty()) return null;

        return SubscriberBO.builder()
                .defaultFlag(false)
                .name(subscriber.getName())
                .organizations(organizations)
                .roles(List.of())
                .id(subscriber.getId())
                .criteria(subscriber.getCriteria())
                .authorizedDomains(subscriber.getAuthorizedDomains())
                .build();
    }

    /**
     * Build organization.
     *
     * @param userOrganization the user's organization.
     * @return the user's organization.
     */
    public OrganizationBO buildOrganization(final UserOrganization userOrganization) {
        if (userOrganization.getRoles() == null || userOrganization.getRoles().isEmpty()) return null;

        return OrganizationBO.builder()
                .roles(userOrganization.getRoles().stream().map(Role::getName).toList())
                .defaultFlag(userOrganization.getDefaultFlag())
                .name(userOrganization.getOrganization().getName())
                .id(userOrganization.getOrganization().getId())
                .status(userOrganization.getOrganization().getStatus())
                .deletionDate(userOrganization.getOrganization().getDeletionDate())
                .criteriaIs(userOrganization.getOrganization().getCriteriaIs())
                .criteriaDs(userOrganization.getOrganization().getCriteriaDs())
                .build();
    }


    /**
     * Clear user cache
     *
     * @param user the user.
     */
    public void clearUserCache(final UserBO user) {
        List.of(false, true).forEach(isAdmin -> Objects.requireNonNull(cacheManager.getCache(USER))
                .evict(UserBO.builder().email(user.getEmail()).adminMode(isAdmin).build()));

    }

    /**
     * Clear user cache
     *
     * @param user the user.
     */
    public void clearUserCache(final UserBO user, final String subscriber, Long organization) {
        List.of(false, true).forEach(isAdmin -> Objects.requireNonNull(cacheManager.getCache(USER))
                .evict(UserBO.builder().email(user.getEmail()).adminMode(isAdmin).build()));
        Objects.requireNonNull(cacheManager.getCache(TOKEN))
                .evict(user.getEmail() + subscriber + organization);
    }

    /**
     * Clear user cache
     */
    public void clearUserAllCache() {
        Objects.requireNonNull(cacheManager.getCache(USER)).clear();
        Objects.requireNonNull(cacheManager.getCache(TOKEN)).clear();
    }

}
