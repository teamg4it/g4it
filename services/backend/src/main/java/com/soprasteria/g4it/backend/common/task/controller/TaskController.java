/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.task.controller;

import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.server.gen.api.TaskApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.TaskRest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Task Rest Controller
 */
@Service
@NoArgsConstructor
public class TaskController implements TaskApiDelegate {

    @Autowired
    TaskService taskService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<TaskRest> getTask(final String organization,
                                            final Long workspace,
                                            final Long taskId
    ) {
        return ResponseEntity.ok(taskService.getTask(taskId));
    }

}
