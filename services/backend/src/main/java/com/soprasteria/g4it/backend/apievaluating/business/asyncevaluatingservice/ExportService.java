/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ExportService {

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @Autowired
    LocalFileService localFileService;

    @Autowired
    private FileSystem fileSystem;

    /**
     * Create export directory for task id
     *
     * @param taskId the task id
     */
    public Path createExportDirectory(Long taskId) {
        Path exportDirectory = Path.of(localWorkingFolder).resolve("export").resolve(String.valueOf(taskId));

        try {
            Files.createDirectories(exportDirectory);
        } catch (IOException e) {
            throw new AsyncTaskException("Cannot create export directory", e);
        }

        return exportDirectory;
    }

    /**
     * Create an export zip file name $taskId.zip from localWorkingFolder/export/$taskId
     * Upload this zip into FileStorage export/$taskId.zip
     *
     * @param taskId         the taskId
     * @param subscriber     the subscriber
     * @param organizationId the organization id as string
     */
    public void uploadExportZip(Long taskId, String subscriber, String organizationId) {
        FileStorage fileStorage = fileSystem.mount(subscriber, organizationId);
        try {
            final Path exportPath = Path.of(localWorkingFolder).resolve("export").resolve(String.valueOf(taskId));
            if (Files.exists(exportPath) && !localFileService.isEmpty(exportPath)) {
                // create rejected zip file
                final File exportZipFile = localFileService.createZipFile(exportPath, exportPath.resolve(taskId + Constants.ZIP).toString());

                // send zip to file storage
                fileStorage.upload(exportZipFile.getAbsolutePath(), FileFolder.EXPORT, taskId + Constants.ZIP);

                // clear directory
                FileSystemUtils.deleteRecursively(exportPath);
            }
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on file upload of rejected zip file", e);
        }
    }


    /**
     * Clean local directory for export
     *
     * @param taskId the task id
     */
    public void clean(Long taskId) {
        try {
            FileSystemUtils.deleteRecursively(Path.of(localWorkingFolder).resolve("export").resolve(String.valueOf(taskId)));
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on cleaning files in local file storage", e);
        }
    }

    /**
     * Clean file storage task export
     *
     * @param taskId the task id
     */
    public void cleanExport(Long taskId, String subscriber, String organizationId) {
        FileStorage fileStorage = fileSystem.mount(subscriber, organizationId);
        try {
            fileStorage.delete(FileFolder.EXPORT, taskId + Constants.ZIP);
        } catch (IOException e) {
            throw new AsyncTaskException("An error occurred on cleaning files in external file storage", e);
        }
    }
}
