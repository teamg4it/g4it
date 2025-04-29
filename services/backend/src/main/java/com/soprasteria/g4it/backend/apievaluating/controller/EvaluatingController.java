/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apievaluating.controller;

import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.common.task.mapper.TaskMapper;
import com.soprasteria.g4it.backend.server.gen.api.InventoryEvaluatingApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Inventory Loading Rest Service.
 */
@Service
@NoArgsConstructor
public class EvaluatingController implements InventoryEvaluatingApiDelegate {

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    EvaluatingService evaluatingService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> launchEvaluating(final String subscriber,
                                                       final Long organization,
                                                       final Long inventoryId,
                                                       String acceptLanguage
    ) {
        return ResponseEntity.ok(taskMapper.mapTaskId(
                evaluatingService.evaluating(
                        subscriber, organization, inventoryId
                )
        ));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> launchEvaluatingDigitalService(final String subscriber,
                                                                     final Long organization,
                                                                     final String digitalServiceUid,
                                                                     String acceptLanguage
    ) {
        return ResponseEntity.ok(taskMapper.mapTaskId(
                evaluatingService.evaluatingDigitalService(
                        subscriber, organization, digitalServiceUid
                )
        ));
    }
}
