/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@Slf4j
public class AdministratorActionsService {


    @Autowired
    TaskRepository taskRepository;

    @Autowired
    @Qualifier("taskExecutorSingleThreaded")
    TaskExecutor taskExecutor;

    @Autowired
    AsyncMigratingService asyncMigratingService;

    public Long migrateToNewTables() {
        log.info("Create task for migrating to new tables");

        // run loading async task
        Context context = Context.builder()
                .datetime(LocalDateTime.now())
                .build();

        // create task with type LOADING
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.MIGRATING_NEW_TABLE.toString())
                .build();

        taskRepository.save(task);

        taskExecutor.execute(new BackgroundTask(context, task, asyncMigratingService));

        return task.getId();
    }


}