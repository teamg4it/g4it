/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.controller;

import com.soprasteria.g4it.backend.apireferential.business.ReferentialExportService;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialImportService;
import com.soprasteria.g4it.backend.server.gen.api.ReferentialImportExportApiDelegate;
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
public class ReferentialImportExportController implements ReferentialImportExportApiDelegate {

    @Autowired
    private ReferentialExportService referentialExportService;
    @Autowired
    private ReferentialImportService referentialImportService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> exportReferentialCSV(String type, String organization) {
        try {
            InputStream inputStream = referentialExportService.exportReferentialToCSV(type, organization);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while downloading file: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ImportReportRest> importReferentialCSV(String type, MultipartFile file, String organization) {

        ImportReportRest importReportRest = referentialImportService.importReferentialCSV(type, file, organization);

        return new ResponseEntity<>(importReportRest, HttpStatus.OK);
    }
}


