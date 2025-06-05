/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.controller;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceIndicatorApiDelegate;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Digital Service Indicator Rest Service.
 */
@Service
public class DigitalServiceIndicatorController implements DigitalServiceIndicatorApiDelegate {

    @Value("${local.working.folder}")
    private String localWorkingFolder;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private FileSystemService fileSystemService;

    @PostConstruct
    public void initFolder() throws IOException {
        Files.createDirectories(Path.of(localWorkingFolder, "export", "digital-service"));
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> getDigitalServiceIndicatorsExportResult(String subscriber,
                                                                            Long organization,
                                                                            String digitalServiceUid) {
        Task task = taskRepository.findByDigitalServiceUid(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", "Digital service task not found"));
        String filename = task.getId() + Constants.ZIP;

        final String filePath = String.join("/", subscriber, organization.toString(), FileFolder.EXPORT.getFolderName(), filename);

        try {
            InputStream inputStream =  fileSystemService.downloadFile(subscriber, organization, FileFolder.EXPORT, filename);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (BlobStorageException e) {
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                throw new G4itRestException("404", String.format("file %s not found in filestorage", filePath));
            } else {
                throw new G4itRestException("500", String.format("Something went wrong downloading file %s", filePath), e);
            }
        } catch (FileNotFoundException e) {
            throw new G4itRestException("404", String.format("file %s not found in filestorage", filePath));
        } catch (IOException e) {
            throw new G4itRestException("500", String.format("Something went wrong downloading file %s", filePath), e);
        }
    }


}