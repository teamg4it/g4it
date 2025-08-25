/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.WorkspaceRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileDeletionService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.WorkspaceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StorageDeletionService {

    @Value("${g4it.storage.retention.day.export}")
    private Integer storageRetentionDayExport;
    @Value("${g4it.storage.retention.day.output}")
    private Integer storageRetentionDayOutput;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private FileDeletionService fileDeletionService;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Execute the deletion
     * Get all subscribers and organizations
     * Execute the deletion for output and work folders
     */
    public void executeDeletion() {

        final long start = System.currentTimeMillis();
        // Fetch Organization with 'ACTIVE' status only.
        List<Workspace> workspaces = workspaceRepository.findAllByStatusIn(List.of(WorkspaceStatus.ACTIVE.name()));

        List<String> deletedFilePaths = new ArrayList<>();
        for (Workspace workspaceEntity : workspaces) {
            final String subscriber = workspaceEntity.getOrganization().getName();
            final Long organizationId = workspaceEntity.getId();

            // organization > subscriber > default
            final Integer retentionExport = Optional.ofNullable(workspaceEntity.getStorageRetentionDayExport())
                    .orElse(Optional.ofNullable(workspaceEntity.getOrganization().getStorageRetentionDayExport())
                            .orElse(storageRetentionDayExport));

            List<String> deletedExportFilePaths = fileDeletionService.deleteFiles(subscriber, organizationId.toString(), FileFolder.EXPORT, retentionExport);

            deletedFilePaths.addAll(deletedExportFilePaths);
            // organization > subscriber > default
            final Integer retentionOutput = Optional.ofNullable(workspaceEntity.getStorageRetentionDayOutput())
                    .orElse(Optional.ofNullable(workspaceEntity.getOrganization().getStorageRetentionDayOutput())
                            .orElse(storageRetentionDayOutput));

            List<String> deletedOutputFilePaths = fileDeletionService.deleteFiles(subscriber, organizationId.toString(), FileFolder.OUTPUT, retentionOutput);
            deletedFilePaths.addAll(deletedOutputFilePaths);
        }

        log.info("Deletion of {} files - {}, execution time={} ms", deletedFilePaths.size(), deletedFilePaths, System.currentTimeMillis() - start);
    }

}
