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
     * Repository to access organization data.
     */
    @Autowired
    OrganizationRepository organizationRepository;
    /**
     * Repository to access user data.
     */
    UserRepository userRepository;
    /**
     * Organization Mapper.
     */
    @Autowired
    OrganizationRestMapper organizationRestMapper;
    /**
     * The Administrator Role Service
     */
    @Autowired
    private AdministratorRoleService administratorRoleService;
    /**
     * The Organization Service
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * The User Service
     */
    @Autowired
    private UserService userService;

    /**
     * Retrieve the list of organizations for the user which has ROLE_ORGANIZATION_ADMINISTRATOR on it
     *
     * @param user the user.
     * @return the List<OrganizationBO>.
     */
    public List<OrganizationBO> getOrganizations(final UserBO user) {
        administratorRoleService.hasAdminRightsOnAnyOrganizationOrAnyWorkspace(user);

        List<OrganizationBO> resultOrganizationAdmin = user.getOrganizations().stream()
                .filter(organizationBO -> organizationBO.getRoles().contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)).toList();

        List<OrganizationBO> checkWorkspaceAdmin = user.getOrganizations().stream()
                .filter(organizationBO -> organizationBO.getRoles().isEmpty()).toList();
        List<OrganizationBO> result = new ArrayList<>(resultOrganizationAdmin);
        for (OrganizationBO orgBO : checkWorkspaceAdmin) {
            List<WorkspaceBO> workspace = orgBO.getWorkspaces().stream()
                    .filter(workspaceBO -> workspaceBO.getRoles().contains(Constants.ROLE_WORKSPACE_ADMINISTRATOR)).toList();
            if (!workspace.isEmpty()) {
                orgBO.setWorkspaces(workspace);
                result.add(orgBO);
            }
        }
        return result;
    }

    /**
     * @param organizationId the organization id
     * @param criteriaRest   criteria to set
     * @param user           the current user
     * @return the organizationBo with updated criteria
     */
    public OrganizationBO updateOrganizationCriteria(final Long organizationId, final CriteriaRest criteriaRest, final UserBO user) {
        administratorRoleService.hasAdminRightsOnAnyOrganization(user);
        Organization organizationToUpdate = organizationService.getOrgById(organizationId);
        organizationToUpdate.setCriteria(criteriaRest.getCriteria());
        organizationRepository.save(organizationToUpdate);
        userService.clearUserAllCache();
        return organizationRestMapper.toBusinessObject(organizationToUpdate);
    }

    /**
     * Get all the users (filtered by authorized_domains of organization)
     *
     * @param searchedName   the string to be searched
     * @param organizationId the organization's id
     * @param user           the  user
     */
    public List<UserSearchBO> searchUserByName(final String searchedName,
                                               final Long organizationId,
                                               final Long workspaceId,
                                               final UserBO user) {

        administratorRoleService.hasAdminRightOnOrganizationOrWorkspace(user, organizationId, workspaceId);

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("Organization %d not found.", organizationId)));

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
                        userRoles.addAll(searchedUser.getUserWorkspaces().stream().filter(org -> org.getWorkspace().getId() == workspaceId)
                                .findFirst()
                                .orElse(UserWorkspace.builder().roles(List.of()).build())
                                .getRoles().stream().map(Role::getName).toList());
                    }

                    if (searchedUser.getUserOrganizations() != null) {
                        userRoles.addAll(searchedUser.getUserOrganizations().stream()
                                .filter(userOrganization -> userOrganization.getOrganization().getId() == organizationId)
                                .map(userOrganization -> userOrganization.getUserRoleOrganization().stream()
                                        .map(userRoleOrganization -> userRoleOrganization.getRoles().getName())
                                        .toList()
                                )
                                .flatMap(Collection::stream)
                                .toList());
                    }

                    List<Long> linkedWorkIds = searchedUser.getUserWorkspaces() == null ? List.of() :
                            searchedUser.getUserWorkspaces().stream()
                                    .map(userWork -> userWork.getWorkspace().getId())
                                    .toList();

                    return UserSearchBO.builder()
                            .id(searchedUser.getId())
                            .firstName(searchedUser.getFirstName())
                            .lastName(searchedUser.getLastName())
                            .email(searchedUser.getEmail())
                            .linkedWorkIds(linkedWorkIds)
                            .roles(userRoles)
                            .build();
                })
                .toList();
    }

    public OrganizationBO getOrganizationById(Long id) {
        return organizationRestMapper.toBusinessObject(organizationRepository.findById(id).orElse(null));
    }

}
