/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministratoractions.controller;

import com.soprasteria.g4it.backend.apiadministratoractions.business.AdministratorActionsService;
import com.soprasteria.g4it.backend.server.gen.api.AdministratorActionsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Administrator Actions Rest Service.
 */
@Slf4j
@Service
public class AdministratorActionsRestController implements AdministratorActionsApiDelegate {

    @Autowired
    AdministratorActionsService administratorActionsService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> migrateDataToNewFormat() {
        return ResponseEntity.ok(TaskIdRest.builder()
                .taskId(administratorActionsService.migrateToNewTables())
                .build()
        );
    }
}

