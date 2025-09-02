/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
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
    WorkspaceService workspaceService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NewUserService newUserService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

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

        List<OrganizationBO> organizationBOList = Constants.SUPER_ADMIN_EMAIL.equals(email) ?
                buildOrganizationsForSuperAdmin() :
                buildOrganizations(userReturned, userInfo.isAdminMode());

        return UserBO.builder()
                .id(userReturned.getId())
                .firstName(userReturned.getFirstName())
                .lastName(userReturned.getLastName())
                .email(userReturned.getEmail())
                .isSuperAdmin(Constants.SUPER_ADMIN_EMAIL.equals(userReturned.getEmail()))
                .organizations(organizationBOList)
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
                    roleRepository.findByName(Constants.ROLE_DIGITAL_SERVICE_READ)
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
        for (Organization organization : organizationRepository.findByAuthorizedDomainsNotNull()) {
            if (Arrays.asList(organization.getAuthorizedDomains().split(",")).contains(userInfo.getDomain())) {
                Workspace demoOrg = workspaceRepository.findByOrganizationNameAndName(organization.getName(), Constants.DEMO)
                        .orElseGet(() -> workspaceRepository.save(Workspace.builder()
                                .name(Constants.DEMO)
                                .status(WorkspaceStatus.ACTIVE.name())
                                .creationDate(LocalDateTime.now())
                                .organization(organization)
                                .build()));
                newUser = newUserService.createUser(organization, demoOrg, newUser, userInfo, accessRoles);
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
     * @param withOrganizations include organizations
     * @return the userBO
     */
    @Transactional
    public UserBO getNoSecurityUser(boolean withOrganizations) {
        User user = userRepository.findByEmail(Constants.SUPER_ADMIN_EMAIL).orElseThrow();

        return UserBO.builder()
                .id(user.getId())
                .firstName("Admin")
                .lastName("No Security Mode")
                .email(Constants.SUPER_ADMIN_EMAIL)
                .isSuperAdmin(true)
                .organizations(withOrganizations ? buildOrganizationsForSuperAdmin() : null)
                .adminMode(true)
                .domain(Constants.SUPER_ADMIN_EMAIL.split("@")[1])
                .build();
    }

    /**
     * Build organization list.
     *
     * @return the user's organization list.
     */
    public List<OrganizationBO> buildOrganizationsForSuperAdmin() {

        // Get the organizations and subObjects on which the user has ROLE_SUBSCRIBER_ADMINISTRATOR
        return organizationRepository.findAll().stream()
                .map(organization -> {
                    var organizationBO = OrganizationBO.builder()
                            .defaultFlag(false)
                            .name(organization.getName())
                            .workspaces(organization.getWorkspaces().stream()
                                    .map(workspace -> {
                                        WorkspaceBO workspaceBO = WorkspaceBO.builder()
                                                .roles(List.of())
                                                .defaultFlag(false)
                                                .name(workspace.getName())
                                                .id(workspace.getId())
                                                .status(workspace.getStatus())
                                                .deletionDate(workspace.getDeletionDate())
                                                .criteriaIs(workspace.getCriteriaIs())
                                                .criteriaDs(workspace.getCriteriaDs())
                                                .build();
                                        return workspaceBO;
                                    })
                                    .sorted(Comparator.comparing(WorkspaceBO::getName))
                                    .toList())
                            .roles(List.of(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR))
                            .criteria(organization.getCriteria())
                            .authorizedDomains(organization.getAuthorizedDomains())
                            .id(organization.getId())
                            .ecomindai(organization.isEcomindai())
                            .build();
                    return organizationBO;
                })
                .sorted(Comparator.comparing(OrganizationBO::getName))
                .toList();
    }

    /**
     * Build organization list.
     *
     * @param user the user.
     * @return the user's organization list.
     */
    public List<OrganizationBO> buildOrganizations(final User user, final boolean adminMode) {

        if (user.getUserOrganizations() == null || user.getUserWorkspaces() == null) return List.of();

        // Get the organization and subObjects on which the user has ROLE_SUBSCRIBER_ADMINISTRATOR
        List<OrganizationBO> results = new ArrayList<>(user.getUserOrganizations().stream()
                .filter(userOrganization -> userOrganization.getRoles() != null &&
                        userOrganization.getRoles().stream().anyMatch(role -> role.getName().equals(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR)))
                .map(userOrganization -> buildOrganization(userOrganization, adminMode))
                .sorted(Comparator.comparing(OrganizationBO::getName))
                .toList());

        Set<String> adminOrganizations = results.stream().map(OrganizationBO::getName).collect(Collectors.toSet());

        if (user.getUserWorkspaces() == null) return results;

        // (organization, [userWorkspace])
        final Map<Organization, List<UserWorkspace>> workspaceByOrganization = user.getUserWorkspaces().stream()
                .collect(Collectors.groupingBy(e -> e.getWorkspace().getOrganization()));

        // Get the organizations and subObjects on which the user has not ROLE_SUBSCRIBER_ADMINISTRATOR but other roles on workspaces
        results.addAll(workspaceByOrganization.entrySet().stream()
                .filter(entry -> !adminOrganizations.contains(entry.getKey().getName()))
                .map(userWorksByOrg -> buildOrganizationWithUserWorkspaces(userWorksByOrg.getKey(), userWorksByOrg.getValue(), adminMode))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(OrganizationBO::getName))
                .toList());

        return results;
    }

    /**
     * Build organization.
     *
     * @param userOrganization the user organization.
     * @param adminMode        the adminMode
     * @return the user's organization.
     */
    public OrganizationBO buildOrganization(final UserOrganization userOrganization, final boolean adminMode) {

        final List<Role> roles = userOrganization.getRoles() == null ? List.of() : userOrganization.getRoles();
        final List<String> status = adminMode ? Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS : List.of(WorkspaceStatus.ACTIVE.name());

        return OrganizationBO.builder()
                .defaultFlag(userOrganization.getDefaultFlag())
                .name(userOrganization.getOrganization().getName())
                .workspaces(userOrganization.getOrganization().getWorkspaces().stream()
                        .filter(organization -> status.contains(organization.getStatus()))
                        .<WorkspaceBO>map(organization ->
                                WorkspaceBO.builder()
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
                        .sorted(Comparator.comparing(WorkspaceBO::getName))
                        .toList())
                .roles(roles.stream().map(Role::getName).toList())
                .criteria(userOrganization.getOrganization().getCriteria())
                .authorizedDomains(userOrganization.getOrganization().getAuthorizedDomains())
                .id(userOrganization.getOrganization().getId())
                .ecomindai(userOrganization.getOrganization().isEcomindai())
                .build();
    }

    /**
     * Build the organization if any workspace has at least one user role
     *
     * @param organization   the user organization.
     * @param userWorkspaces the user's organization list.
     * @param adminMode      the adminMode
     * @return the user's organization.
     */
    public OrganizationBO buildOrganizationWithUserWorkspaces(final Organization organization, final List<UserWorkspace> userWorkspaces, final boolean adminMode) {

        final List<String> status = adminMode ? Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS : List.of(WorkspaceStatus.ACTIVE.name());

        List<WorkspaceBO> workspaces = userWorkspaces.stream()
                .filter(userOrganization -> status.contains(userOrganization.getWorkspace().getStatus()))
                .map(this::buildWorkspace)
                .filter(Objects::nonNull)
                .toList();

        if (workspaces.isEmpty()) return null;

        return OrganizationBO.builder()
                .defaultFlag(false)
                .name(organization.getName())
                .workspaces(workspaces)
                .roles(List.of())
                .id(organization.getId())
                .criteria(organization.getCriteria())
                .authorizedDomains(organization.getAuthorizedDomains())
                .ecomindai(organization.isEcomindai())
                .build();
    }

    /**
     * Build organization.
     *
     * @param userWorkspace the user's workspace.
     * @return the user's workspace.
     */
    public WorkspaceBO buildWorkspace(final UserWorkspace userWorkspace) {
        if (userWorkspace.getRoles() == null || userWorkspace.getRoles().isEmpty()) return null;

        return WorkspaceBO.builder()
                .roles(userWorkspace.getRoles().stream().map(Role::getName).toList())
                .defaultFlag(userWorkspace.getDefaultFlag())
                .name(userWorkspace.getWorkspace().getName())
                .id(userWorkspace.getWorkspace().getId())
                .status(userWorkspace.getWorkspace().getStatus())
                .deletionDate(userWorkspace.getWorkspace().getDeletionDate())
                .criteriaIs(userWorkspace.getWorkspace().getCriteriaIs())
                .criteriaDs(userWorkspace.getWorkspace().getCriteriaDs())
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
    public void clearUserCache(final UserBO user, final String organization, Long workspace) {
        List.of(false, true).forEach(isAdmin -> Objects.requireNonNull(cacheManager.getCache(USER))
                .evict(UserBO.builder().email(user.getEmail()).adminMode(isAdmin).build()));
        Objects.requireNonNull(cacheManager.getCache(TOKEN))
                .evict(user.getEmail() + organization + workspace);
    }

    /**
     * Clear user cache
     */
    public void clearUserAllCache() {
        Objects.requireNonNull(cacheManager.getCache(USER)).clear();
        Objects.requireNonNull(cacheManager.getCache(TOKEN)).clear();
    }

}
