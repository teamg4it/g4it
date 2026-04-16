/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.controller;

import com.soprasteria.g4it.backend.apireferential.business.WorkspaceReferentialExportService;
import com.soprasteria.g4it.backend.apireferential.business.WorkspaceReferentialImportService;
import com.soprasteria.g4it.backend.server.gen.api.WorkspaceReferentialImportExportApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.ImportReportRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@Service
public class WorkspaceReferentialImportExportController implements WorkspaceReferentialImportExportApiDelegate {

/*    @Autowired
    private WorkspaceReferentialExportService workspaceReferentialExportService;*/

    @Autowired
    private WorkspaceReferentialImportService workspaceReferentialImportService;

    /**
     * {@inheritDoc}
     */
   /* @Override
    public ResponseEntity<Resource> exportWorkspaceReferentialCSV(Long workspaceId, String type) {
        try {
            InputStream inputStream =
                    workspaceReferentialExportService.exportReferentialToCSV(workspaceId, type);

            return ResponseEntity.ok(new InputStreamResource(inputStream));

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while downloading file: " + e.getMessage()
            );
        }
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ImportReportRest> importWorkspaceReferentialCSV(
            Long workspaceId,
            String type,
            MultipartFile file
    ) {

        ImportReportRest report =
                workspaceReferentialImportService.importReferentialCSV(workspaceId, type, file);

        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}