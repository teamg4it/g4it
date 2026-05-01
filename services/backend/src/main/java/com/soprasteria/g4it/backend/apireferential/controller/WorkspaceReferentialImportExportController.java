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

    @Autowired
    private WorkspaceReferentialExportService workspaceReferentialExportService;

    @Autowired
    private WorkspaceReferentialImportService workspaceReferentialImportService;

    /**
     * {@inheritDoc}
     */

   @Override
   public ResponseEntity<Resource> exportWorkspaceReferentialZip(String organization,
                                                                 Long workspace) {

       if (workspace == null) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workspaceId is required");
       }

       try {
           InputStream zipStream =
                   workspaceReferentialExportService.exportReferentialZip(workspace);

           return ResponseEntity.ok()
                   .header("Content-Disposition", "attachment; filename=workspace-referential.zip")
                   .header("Content-Type", "application/zip")
                   .body(new InputStreamResource(zipStream));

       } catch (Exception e) {
           throw new ResponseStatusException(
                   HttpStatus.INTERNAL_SERVER_ERROR,
                   "Error occurred while downloading ZIP: " + e.getMessage()
           );
       }
   }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ImportReportRest> importWorkspaceReferentialCSV(
            String organization,
            Long workspace,
            String type,
            MultipartFile file
    ) {

        ImportReportRest report =
                workspaceReferentialImportService.importReferentialCSV(workspace, type, file);

        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}