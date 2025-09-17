/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.business;

import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FileDeletionService {

    @Autowired
    private FileSystem fileSystem;

    /**
     * Delete files older than storageRetentionday for organization, workspace, fileFolder
     *
     * @param organization          the organization
     * @param workspace        the workspace
     * @param fileFolder          the fileFolder
     * @param storageRetentionDay the storageRetention in days
     */
    public List<String> deleteFiles(String organization, String workspace, FileFolder fileFolder, Integer storageRetentionDay) {

        List<String> filePathsToDelete = new ArrayList<>();

        final OffsetDateTime now = OffsetDateTime.now();

        final FileStorage fileStorage = fileSystem.mount(organization, workspace);
        final String prefix = Path.of(workspace).resolve(fileFolder.getFolderName()).toString();

        log.info("Check deletion for: {}/{}/{}, retention={}", organization, workspace, fileFolder.getFolderName(), storageRetentionDay);
        try {
            final List<String> filesToDelete = fileStorage.listFiles(fileFolder).stream()
                    // date now - n days > createTime
                    .filter(fileDescription ->
                            now.minusDays(storageRetentionDay)
                                    .isAfter(OffsetDateTime.parse(fileDescription.getMetadata().get("creationTime")))
                    )
                    .map(FileDescription::getName)
                    .toList();

            for (String filePath : filesToDelete) {
                log.info("Deleting file: {}", filePath);
                fileStorage.delete(fileFolder, filePath.replace(prefix, ""));
                String deletedFilePath = fileStorage.getFileUrl(fileFolder, filePath.replace(prefix, ""));
                filePathsToDelete.add(deletedFilePath);
            }
        } catch (IOException e) {
            log.error("An error occurred during listing or deleting files", e);
        }
        return filePathsToDelete;
    }

}
