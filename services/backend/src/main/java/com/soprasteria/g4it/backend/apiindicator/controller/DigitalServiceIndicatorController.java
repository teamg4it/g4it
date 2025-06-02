/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.controller;

import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceExportService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceIndicatorApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Digital Service Indicator Rest Service.
 */
@Service
public class DigitalServiceIndicatorController implements DigitalServiceIndicatorApiDelegate {

    /**
     * Export Service
     */
    @Autowired
    private DigitalServiceExportService exportService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> getDigitalServiceIndicatorsExportResult(String subscriber,
                                                                            Long organization,
                                                                            String digitalServiceUid) {
        try {
            InputStream inputStream = exportService.createFiles(digitalServiceUid, subscriber, organization);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while downloading file: " + e.getMessage());
        }
    }


}