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
import com.soprasteria.g4it.backend.server.gen.api.EvaluatingApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskIdRest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Inventory Evaluating end points.
 */
@Service
@NoArgsConstructor
public class EvaluatingController implements EvaluatingApiDelegate {

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    EvaluatingService evaluatingService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> launchEvaluating(final String organization,
                                                       final Long workspace,
                                                       final Long inventoryId,
                                                       String acceptLanguage
    ) {
        return ResponseEntity.ok(taskMapper.mapTaskId(
                evaluatingService.evaluating(
                        organization, workspace, inventoryId
                )
        ));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskIdRest> launchEvaluatingDigitalService(final String organization,
                                                                     final Long workspace,
                                                                     final String digitalServiceVersionUid,
                                                                     String acceptLanguage
    ) {
        return ResponseEntity.ok(taskMapper.mapTaskId(
                evaluatingService.evaluatingDigitalService(
                         organization, workspace, digitalServiceVersionUid
                )
        ));
    }
}
