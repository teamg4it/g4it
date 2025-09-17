/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apifiles.controller;

import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.TemplateFileSystemApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Template Files endpoints.
 */
@Slf4j
@Service
@NoArgsConstructor
public class TemplateFileController implements TemplateFileSystemApiDelegate {

    @Autowired
    private FileSystemService fileSystemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> downloadTemplatesFile(String organization,
                                                          Long workspaceId,
                                                          String name, final String module) {
        FileFolder templateFolder =  FileFolder.IS_TEMPLATE.getFolderName().equals(module) ? FileFolder.IS_TEMPLATE : FileFolder.DS_TEMPLATE;
        final String filePath = String.join(File.separator, String.valueOf(Constants.INTERNAL_WORKSPACE), templateFolder.getFolderName(), name);
        try {
            InputStream inputStream = fileSystemService.downloadFile(Constants.INTERNAL_ORGANIZATION, Constants.INTERNAL_WORKSPACE, templateFolder, name);
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


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<FileDescriptionRest>> getTemplateFiles(final String organization,
                                                                      final Long workspaceId,
                                                                      final String module) {
        try {
            return ResponseEntity.ok().body(fileSystemService.listTemplatesFiles(module));
        } catch (final IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while get file: " + e.getMessage());
        }
    }

}
