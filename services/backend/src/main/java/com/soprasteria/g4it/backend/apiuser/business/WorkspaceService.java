/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.mapper.WorkspaceMapper;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserWorkspaceRepository;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpdateRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Organization service.
 */
@Service
@Slf4j
public class WorkspaceService {

    /**
     * Organization Mapper.
     */
    @Autowired
    WorkspaceMapper workspaceMapper;
    /**
     * Repository to manage user organization.
     */
    @Autowired
    UserWorkspaceRepository userWorkspaceRepository;

    /**
     * Repository to manage user role.
     */
    @Autowired
    UserRoleWorkspaceRepository userRoleWorkspaceRepository;
    /**
     * The Role Service
     */
    @Autowired
    RoleService roleService;

    /**
     * The Subscriber Service
     */
    @Autowired
    OrganizationService organizationService;

    @Autowired
    private CacheManager cacheManager;

    @Value("${g4it.organization.deletion.day}")
    private Integer organizationDataDeletionDays;
    /**
     * The repository to access organization data.
     */
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private FileSystem fileSystem;

    /**
     * Retrieve the active Organization Entity.
     *
     * @param organizationId the organization id.
     * @return the organization.
     */
    @Cacheable("Organization")
    public Workspace getOrganizationById(final Long organizationId) {
        return workspaceRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("organization %d not found", organizationId)));
    }

    /**
     * Retrieve the organization Entity.
     *
     * @param subscriberId   the client subscriber's id.
     * @param organizationId the organization's id.
     * @param status         the organization's statuses.
     * @return the organization.
     */
    public Workspace getOrganizationByStatus(final Long subscriberId, final Long organizationId, List<String> status) {
        Optional<Workspace> optOrg = subscriberId == null ?
                workspaceRepository.findByIdAndStatusIn(organizationId, status) :
                workspaceRepository.findByIdAndOrganizationIdAndStatusIn(organizationId, subscriberId, status);

        return optOrg.orElseThrow(
                () -> new G4itRestException("404", String.format("organization with id '%d' not found", organizationId))
        );
    }

    /**
     * Create an Organization.
     *
     * @param workspaceUpdateRest the organizationUpsertRest.
     * @param user                the user.
     * @param organizationId      the subscriber id.
     * @return organization BO.
     */
    @Transactional
    public WorkspaceBO createWorkspace(WorkspaceUpdateRest workspaceUpdateRest, UserBO user, Long organizationId) {

        // Check if organization with same name already exist on this subscriber.
        workspaceRepository.findByOrganizationIdAndName(workspaceUpdateRest.getOrganizationId(), workspaceUpdateRest.getName())
                .ifPresent(organization -> {
                    throw new G4itRestException("409", String.format("organization '%s' already exists in subscriber '%s'", workspaceUpdateRest.getName(), organizationId));
                });

        // Create organization
        final Workspace workspaceToCreate = workspaceMapper.toEntity(
                workspaceUpdateRest.getName(),
                organizationService.getSubscriptionById(organizationId),
                User.builder().id(user.getId()).build(),
                WorkspaceStatus.ACTIVE.name()
        );
        workspaceToCreate.setIsMigrated(true);
        workspaceRepository.save(workspaceToCreate);

        return workspaceMapper.toBusinessObject(workspaceToCreate);
    }

    /**
     * Update the organization.
     *
     * @param workspaceUpdateRest the WorkspaceUpdateRest.
     * @param userId              the user id.
     * @return OrganizationBO
     */
    @Transactional
    public WorkspaceBO updateWorkspace(final Long organizationId, final WorkspaceUpdateRest workspaceUpdateRest, Long userId) {

        final Workspace workspaceToSave = getOrganizationByStatus(workspaceUpdateRest.getOrganizationId(), organizationId, Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS);

        final String currentStatus = workspaceToSave.getStatus();
        final String newStatus = workspaceUpdateRest.getStatus().name();

        if (currentStatus.equals(WorkspaceStatus.ACTIVE.name()) && newStatus.equals(WorkspaceStatus.ACTIVE.name())) {
            updateNameOrCriteria(workspaceUpdateRest, workspaceToSave);

        } else {
            Integer dataDeletionDays = null;
            LocalDateTime deletionDate = null;

            // Case current organization is ACTIVE and update it to TO_BE_DELETED
            if (currentStatus.equals(WorkspaceStatus.ACTIVE.name()) && newStatus.equals(WorkspaceStatus.TO_BE_DELETED.name())) {
                // Get data retention days
                dataDeletionDays = workspaceUpdateRest.getDataRetentionDays() == null ?
                        organizationDataDeletionDays :
                        workspaceUpdateRest.getDataRetentionDays().intValue();

                deletionDate = LocalDateTime.now().plusDays(dataDeletionDays.longValue());
            }

            workspaceToSave.setDeletionDate(deletionDate);
            workspaceToSave.setDataRetentionDay(dataDeletionDays);
            workspaceToSave.setStorageRetentionDayExport(dataDeletionDays);
            workspaceToSave.setStorageRetentionDayOutput(dataDeletionDays);
            workspaceToSave.setStatus(workspaceUpdateRest.getStatus().name());
        }
        workspaceToSave.setLastUpdatedBy(User.builder()
                .id(userId)
                .build());
        workspaceToSave.setLastUpdateDate(LocalDateTime.now());
        workspaceRepository.save(workspaceToSave);
        clearWorkspaceCache(organizationId);
        return workspaceMapper.toBusinessObject(workspaceToSave);
    }

    /**
     * Update the organization's name or criteria
     *
     * @param workspaceUpdateRest the organizationUpsertRest
     * @param workspaceToSave     the updated organization
     */
    private void updateNameOrCriteria(WorkspaceUpdateRest workspaceUpdateRest, Workspace workspaceToSave) {
        final String currentWorkspace = workspaceToSave.getName();
        final String newWorkspace = workspaceUpdateRest.getName();

        final List<String> currentCriteriaDs = workspaceToSave.getCriteriaDs();
        final List<String> newCriteriaDs = workspaceUpdateRest.getCriteriaDs();

        final List<String> currentCriteriaIs = workspaceToSave.getCriteriaIs();
        final List<String> newCriteriaIs = workspaceUpdateRest.getCriteriaIs();
        boolean isCriteriaChange = !Objects.equals(newCriteriaDs, currentCriteriaDs) || !Objects.equals(newCriteriaIs, currentCriteriaIs);
        boolean isNameChange = !currentWorkspace.equals(newWorkspace);
        if (!(isCriteriaChange || isNameChange)) {
            log.info("Nothing to update in the organization '{}'", workspaceToSave.getId());
            return;
        }
        // Set criteria for organization
        if (isCriteriaChange) {
            workspaceToSave.setCriteriaDs(newCriteriaDs);
            workspaceToSave.setCriteriaIs(newCriteriaIs);
        }
        if (isNameChange) {
            // Handle update in organization's name
            // Check if organization with same name already exist on this subscriber.
            workspaceRepository.findByOrganizationIdAndName(workspaceUpdateRest.getOrganizationId(), newWorkspace)
                    .ifPresent(org -> {
                        throw new G4itRestException("409", String.format("organization '%s' already exists in subscriber '%s'", newWorkspace, workspaceUpdateRest.getOrganizationId()));
                    });

            log.info("Update Organization name in file system from '{}' to '{}'", currentWorkspace, newWorkspace);
            workspaceToSave.setName(newWorkspace);
        }
    }

    /**
     * clear cache to get the updated criteria
     */
    public void clearWorkspaceCache(Long workspaceId) {
        Objects.requireNonNull(cacheManager.getCache("Organization")).evict(workspaceId);
    }
}
