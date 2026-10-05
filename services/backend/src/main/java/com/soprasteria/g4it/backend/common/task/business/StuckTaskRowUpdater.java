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
import com.soprasteria.g4it.backend.common.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of processing a single task during the stuck-task check.
 */
enum TaskCheckResult {
    INITIALIZED, UPDATED, FAILED, NONE
}

/**
 * Applies the stuck-task check/update to a single {@link Task} in its own,
 * short-lived transaction.
 * <p>
 * This is a dedicated Spring bean (rather than a private method on
 * {@link StuckTaskCleanupService}) so that {@code @Transactional(REQUIRES_NEW)}
 * is honoured through Spring's proxy - self-invocation within the same class
 * would silently bypass the transactional proxy.
 * <p>
 * Using one transaction per task (instead of one transaction for the whole
 * scheduler run) ensures the row lock taken by the UPDATE is released
 * immediately after each task is processed, instead of being held until every
 * IN_PROGRESS task in the system has been checked. Holding it longer was
 * causing lock contention with in-flight loading/evaluating tasks (e.g.
 * {@code LoadInputFilesService} / {@code AsyncLoadFilesService}) that
 * frequently update their own task row, significantly slowing down those
 * flows whenever the scheduler was running concurrently.
 */
@Component
@Slf4j
public class StuckTaskRowUpdater {

    private static final String FAILED_BY_SCHEDULER = "FAILED BY SCHEDULER";

    @Autowired
    private TaskRepository taskRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TaskCheckResult processTask(final Task task, final LocalDateTime now) {
        LocalDateTime lastUpdate = task.getLastUpdateDate();
        LocalDateTime progressLastChanged = task.getProgressLastChangedDate();

        // Truncate to seconds to avoid precision issues when comparing
        lastUpdate = lastUpdate.truncatedTo(ChronoUnit.SECONDS);
        if (progressLastChanged != null) {
            progressLastChanged = progressLastChanged.truncatedTo(ChronoUnit.SECONDS);
        }

        // Case 1: PLCD is null - First scheduler check, initialize and skip
        if (progressLastChanged == null) {
            try {
                taskRepository.updateProgressLastChangedDate(task.getId(), lastUpdate);
                log.info("Task {} - First check, initialized PLCD = LUD", task.getId());
                return TaskCheckResult.INITIALIZED;
            } catch (Exception e) {
                log.error("Error while initializing progressLastChangedDate for task {}: {}",
                        task.getId(), e.getMessage(), e);
                return TaskCheckResult.NONE;
            }
        }

        // Case 2: LUD > PLCD - Task has progressed, update and skip
        if (lastUpdate.isAfter(progressLastChanged)) {
            try {
                taskRepository.updateProgressLastChangedDate(task.getId(), lastUpdate);
                log.info("Task {} - Progress detected, updated PLCD = LUD", task.getId());
                return TaskCheckResult.UPDATED;
            } catch (Exception e) {
                log.error("Error while updating progressLastChangedDate for task {}: {}",
                        task.getId(), e.getMessage(), e);
                return TaskCheckResult.NONE;
            }
        }

        // Case 3: LUD == PLCD - Task is stuck, KILL it
        if (lastUpdate.equals(progressLastChanged) || lastUpdate.isBefore(progressLastChanged)) {
            long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(task.getProgressLastChangedDate(), now);
            log.info("Task {} (type: {}) is STUCK - LUD == PLCD, no updates for {} minutes",
                    task.getId(), task.getType(), minutesSinceLastUpdate);
            failTask(task, now, minutesSinceLastUpdate);
            return TaskCheckResult.FAILED;
        }

        return TaskCheckResult.NONE;
    }

    /**
     * Mark a task as FAILED with appropriate error message and details.
     *
     * @param task                 the task to fail
     * @param now                  the current timestamp
     * @param minutesWithoutUpdate minutes since last update
     */
    private void failTask(Task task, LocalDateTime now, long minutesWithoutUpdate) {
        try {
            // Create error message
            String errorMessage = String.format(
                    "Task has been stuck with no updates from %d minutes and has been automatically terminated.",
                    minutesWithoutUpdate
            );

            // Update task details - LogUtils.error/info automatically truncate to fit database limit
            List<String> details = task.getDetails() != null ? new ArrayList<>(task.getDetails()) : new ArrayList<>();
            details.add(LogUtils.error(errorMessage));

            // Set errors - truncate raw message to fit database limit
            List<String> errors = new ArrayList<>();
            errors.add(LogUtils.error(FAILED_BY_SCHEDULER));

            // Update task status via a targeted update to avoid rewriting the whole entity
            // (and avoid triggering the eager "createdBy" relation load) for every stuck task.
            taskRepository.updateStuckTaskFailed(task.getId(), TaskStatus.FAILED.toString(), now, details, errors);

            log.info("Task {} (type: {}) marked as FAILED",
                    task.getId(), task.getType());

        } catch (Exception e) {
            log.error("Error while failing stuck task {}: {}", task.getId(), e.getMessage(), e);
        }
    }
}
