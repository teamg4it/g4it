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
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpsertRest;
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
 * Workspace service.
 */
@Service
@Slf4j
public class WorkspaceService {

    /**
     * Workspace Mapper.
     */
    @Autowired
    WorkspaceMapper workspaceMapper;
    /**
     * The Organization Service
     */
    @Autowired
    OrganizationService organizationService;

    @Autowired
    private CacheManager cacheManager;

    @Value("${g4it.workspace.deletion.day}")
    private Integer workspaceDataDeletionDays;
    /**
     * The repository to access workspace data.
     */
    @Autowired
    private WorkspaceRepository workspaceRepository;

    /**
     * Retrieve the active Workspace Entity.
     *
     * @param workspaceId the workspace id.
     * @return the workspace.
     */
    @Cacheable("Workspace")
    public Workspace getWorkspaceById(final Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new G4itRestException("404", String.format("workspace %d not found", workspaceId)));
    }

    /**
     * Retrieve the Workspace Entity.
     *
     * @param organizationId   the client organization's id.
     * @param workspaceId the workspace id.
     * @param status         the workspace statuses.
     * @return the workspace.
     */
    public Workspace getWorkspaceByStatus(final Long organizationId, final Long workspaceId, List<String> status) {
        Optional<Workspace> optWork = organizationId == null ?
                workspaceRepository.findByIdAndStatusIn(workspaceId, status) :
                workspaceRepository.findByIdAndOrganizationIdAndStatusIn(workspaceId, organizationId, status);

        return optWork.orElseThrow(
                () -> new G4itRestException("404", String.format("workspace with id '%d' not found", workspaceId))
        );
    }

    /**
     * Create a Workspace.
     *
     * @param workspaceUpsertRest the workspaceUpsertRest.
     * @param user                the user.
     * @param organizationId      the organization id.
     * @return Workspace BO.
     */
    @Transactional
    public WorkspaceBO createWorkspace(WorkspaceUpsertRest workspaceUpsertRest, UserBO user, Long organizationId) {

        // Check if workspace with same name already exist on this organization.
        workspaceRepository.findByOrganizationIdAndName(workspaceUpsertRest.getOrganizationId(), workspaceUpsertRest.getName())
                .ifPresent(workspace -> {
                    throw new G4itRestException("409", String.format("workspace '%s' already exists in organization '%s'", workspaceUpsertRest.getName(), organizationId));
                });

        // Create Workspace
        final Workspace workspaceToCreate = workspaceMapper.toEntity(
                workspaceUpsertRest.getName(),
                organizationService.getOrgById(organizationId),
                User.builder().id(user.getId()).build(),
                WorkspaceStatus.ACTIVE.name()
        );
        workspaceToCreate.setIsMigrated(true);
        workspaceRepository.save(workspaceToCreate);

        return workspaceMapper.toBusinessObject(workspaceToCreate);
    }

    /**
     * Update the Workspace.
     *
     * @param workspaceUpsertRest the WorkspaceUpsertRest.
     * @param userId              the user id.
     * @return WorkspaceBO
     */
    @Transactional
    public WorkspaceBO updateWorkspace(final Long workspaceId, final WorkspaceUpsertRest workspaceUpsertRest, Long userId) {

        final Workspace workspaceToSave = getWorkspaceByStatus(workspaceUpsertRest.getOrganizationId(), workspaceId, Constants.WORKSPACE_ACTIVE_OR_DELETED_STATUS);

        final String currentStatus = workspaceToSave.getStatus();
        final String newStatus = workspaceUpsertRest.getStatus().name();

        if (currentStatus.equals(WorkspaceStatus.ACTIVE.name()) && newStatus.equals(WorkspaceStatus.ACTIVE.name())) {
            updateNameOrCriteria(workspaceUpsertRest, workspaceToSave);

        } else {
            Integer dataDeletionDays = null;
            LocalDateTime deletionDate = null;

            // Case current workspace is ACTIVE and update it to TO_BE_DELETED
            if (currentStatus.equals(WorkspaceStatus.ACTIVE.name()) && newStatus.equals(WorkspaceStatus.TO_BE_DELETED.name())) {
                // Get data retention days
                dataDeletionDays = workspaceUpsertRest.getDataRetentionDays() == null ?
                        workspaceDataDeletionDays :
                        workspaceUpsertRest.getDataRetentionDays().intValue();

                deletionDate = LocalDateTime.now().plusDays(dataDeletionDays.longValue());
            }

            workspaceToSave.setDeletionDate(deletionDate);
            workspaceToSave.setDataRetentionDay(dataDeletionDays);
            workspaceToSave.setStorageRetentionDayExport(dataDeletionDays);
            workspaceToSave.setStorageRetentionDayOutput(dataDeletionDays);
            workspaceToSave.setStatus(workspaceUpsertRest.getStatus().name());
        }
        workspaceToSave.setLastUpdatedBy(User.builder()
                .id(userId)
                .build());
        workspaceToSave.setLastUpdateDate(LocalDateTime.now());
        workspaceRepository.save(workspaceToSave);
        clearWorkspaceCache(workspaceId);
        return workspaceMapper.toBusinessObject(workspaceToSave);
    }

    /**
     * Update the workspace name or criteria
     *
     * @param workspaceUpsertRest the workspaceUpsertRest
     * @param workspaceToSave     the updated Workspace
     */
    private void updateNameOrCriteria(WorkspaceUpsertRest workspaceUpsertRest, Workspace workspaceToSave) {
        final String currentWorkspace = workspaceToSave.getName();
        final String newWorkspace = workspaceUpsertRest.getName();

        final List<String> currentCriteriaDs = workspaceToSave.getCriteriaDs();
        final List<String> newCriteriaDs = workspaceUpsertRest.getCriteriaDs();

        final List<String> currentCriteriaIs = workspaceToSave.getCriteriaIs();
        final List<String> newCriteriaIs = workspaceUpsertRest.getCriteriaIs();
        boolean isCriteriaChange = !Objects.equals(newCriteriaDs, currentCriteriaDs) || !Objects.equals(newCriteriaIs, currentCriteriaIs);
        boolean isNameChange = !currentWorkspace.equals(newWorkspace);
        if (!(isCriteriaChange || isNameChange)) {
            log.info("Nothing to update in the workspace '{}'", workspaceToSave.getId());
            return;
        }
        // Set criteria for workspace
        if (isCriteriaChange) {
            workspaceToSave.setCriteriaDs(newCriteriaDs);
            workspaceToSave.setCriteriaIs(newCriteriaIs);
        }
        if (isNameChange) {
            // Handle update in workspace name
            // Check if workspace with same name already exist on this organization.
            workspaceRepository.findByOrganizationIdAndName(workspaceUpsertRest.getOrganizationId(), newWorkspace)
                    .ifPresent(org -> {
                        throw new G4itRestException("409", String.format("workspace '%s' already exists in organization '%s'", newWorkspace, workspaceUpsertRest.getOrganizationId()));
                    });

            log.info("Update workspace name in file system from '{}' to '{}'", currentWorkspace, newWorkspace);
            workspaceToSave.setName(newWorkspace);
        }
    }

    /**
     * clear cache to get the updated criteria
     */
    public void clearWorkspaceCache(Long workspaceId) {
        Objects.requireNonNull(cacheManager.getCache("Workspace")).evict(workspaceId);
    }
}
