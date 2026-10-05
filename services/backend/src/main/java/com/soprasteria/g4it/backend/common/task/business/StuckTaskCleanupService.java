/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.business;

import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to detect and fail tasks that are stuck in IN_PROGRESS status.
 *
 * This service monitors all tasks and marks them as FAILED if they:
 * - Have been IN_PROGRESS for longer than the maximum allowed timeout
 * - Haven't been updated for longer than the stuck task threshold
 *
 * This prevents zombie tasks and provides clear feedback to users.
 */
@Service
@Slf4j
public class StuckTaskCleanupService {

    @Value("${g4it.task.stuck.check.enabled:true}")
    private boolean stuckTaskCheckEnabled;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StuckTaskRowUpdater stuckTaskRowUpdater;

    /**
     * Find and fail all tasks that are stuck in IN_PROGRESS status.
     *
     * Logic:
     * - If PLCD is null: Initialize PLCD = LUD (first time, task is progressing)
     * - If LUD > PLCD: Task has progressed, update PLCD = LUD
     * - If LUD == PLCD: Task is stuck (no updates since last check), KILL it
     *
     * Where PLCD = progressLastChangedDate, LUD = lastUpdateDate
     */
    public void failStuckTasks() {
        if (!stuckTaskCheckEnabled) {
            log.info("Stuck task check is disabled");
            return;
        }

        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString());

        if (inProgressTasks.isEmpty()) {
            log.info("No IN_PROGRESS tasks found");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int failedCount = 0;
        int initializedCount = 0;
        int updatedCount = 0;

        for (Task task : inProgressTasks) {
            // Delegate to a separate bean so each task is processed in its own
            // short-lived REQUIRES_NEW transaction (via the injected Spring proxy).
            // This releases the row lock immediately instead of holding it for
            // the whole loop, which previously caused lock contention with
            // in-flight loading/evaluating tasks updating their own task row.
            TaskCheckResult result = stuckTaskRowUpdater.processTask(task, now);
            switch (result) {
                case INITIALIZED -> initializedCount++;
                case UPDATED -> updatedCount++;
                case FAILED -> failedCount++;
                case NONE -> { /* nothing to do */ }
            }
        }

        if (failedCount > 0 || initializedCount > 0 || updatedCount > 0) {
            log.info("Stuck task cleanup completed - {} initialized, {} updated, {} KILLED",
                    initializedCount, updatedCount, failedCount);
        } else {
            log.info("All IN_PROGRESS tasks are healthy");
        }
    }
}



