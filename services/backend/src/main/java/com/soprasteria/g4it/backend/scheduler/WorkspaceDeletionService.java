/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WorkspaceDeletionService {

    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private DigitalServiceRepository digitalServiceRepository;
    @Autowired
    private InventoryDeleteService inventoryDeleteService;
    @Autowired
    private DigitalServiceService digitalServiceService;

    @Autowired
    private FileDeletionService fileDeletionService;

    /**
     * Execute the deletion
     * Get all workspaces with status 'TO_BE_DELETED'
     * Execute the deletion for data and storage files
     */
    public void executeDeletion() {
        final long start = System.currentTimeMillis();
        final LocalDateTime now = LocalDateTime.now();
        int nbInventoriesDeleted = 0;
        int nbDigitalServicesDeleted = 0;
        List<String> deletedFilePaths = new ArrayList<>();

        List<Workspace> workspaces = workspaceRepository.findAllByStatusIn(List.of(WorkspaceStatus.TO_BE_DELETED.name()));

        for (Workspace workspaceEntity : workspaces) {
            final String organization = workspaceEntity.getOrganization().getName();
            final Long workspaceId = workspaceEntity.getId();

            if (workspaceEntity.getDeletionDate() == null) {
                log.error("Workspace {} has {} status and deletion date NULL", workspaceId, WorkspaceStatus.TO_BE_DELETED);
                continue;
            }

            final int dataRetentionDay = now.isAfter(workspaceEntity.getDeletionDate()) ? 0 : -1;
            if (dataRetentionDay == 0) {
                log.info("Deleting data of {}/{}", organization, workspaceEntity.getName());
                // Delete Inventories
                nbInventoriesDeleted += inventoryRepository.findByWorkspace(workspaceEntity).stream()
                        .mapToInt(inventory -> {
                            inventoryDeleteService.deleteInventory(organization, workspaceId, inventory.getId());
                            return 1;
                        })
                        .sum();

                // Delete Digital services
                nbDigitalServicesDeleted += digitalServiceRepository.findByWorkspace(workspaceEntity).stream()
                        .mapToInt(digitalServiceBO -> {
                            digitalServiceService.deleteDigitalService(digitalServiceBO.getUid());
                            return 1;
                        })
                        .sum();

                // Delete Export Files from storage
                List<String> deletedExportFilePaths = fileDeletionService.deleteFiles(organization, workspaceId.toString(), FileFolder.EXPORT, dataRetentionDay);

                deletedFilePaths.addAll(deletedExportFilePaths);

                // Delete Output Files from storage
                List<String> deletedOutputFilePaths = fileDeletionService.deleteFiles(organization, workspaceId.toString(), FileFolder.OUTPUT, dataRetentionDay);
                deletedFilePaths.addAll(deletedOutputFilePaths);

                // update workspace status to "INACTIVE" if status is "TO_BE_DELETED"
                if (workspaceEntity.getStatus().equals(WorkspaceStatus.TO_BE_DELETED.name())) {
                    workspaceRepository.setStatusForWorkspace(workspaceEntity.getId(), WorkspaceStatus.INACTIVE.name());
                    log.info("Update status of {}/{} to {} ", organization, workspaceEntity.getName(), WorkspaceStatus.INACTIVE.name());
                }
            }
        }
        log.info("Deletion of {} inventories , {} digital-services  and {} files - {} , execution time={} ms for workspace marked as {}",
                nbInventoriesDeleted,
                nbDigitalServicesDeleted,
                deletedFilePaths.size(),
                deletedFilePaths,
                System.currentTimeMillis() - start,
                WorkspaceStatus.TO_BE_DELETED.name()
        );
    }

}
