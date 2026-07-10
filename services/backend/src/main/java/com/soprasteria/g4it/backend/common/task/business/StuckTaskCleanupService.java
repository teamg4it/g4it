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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    @Value("${g4it.task.stuck.timeout.hours:2}")
    private double stuckTaskTimeoutHours;

    @Value("${g4it.task.stuck.check.enabled:true}")
    private boolean stuckTaskCheckEnabled;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MessageSource messageSource;

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
    @Transactional
    public void failStuckTasks() {
        if (!stuckTaskCheckEnabled) {
            log.debug("Stuck task check is disabled");
            return;
        }

        log.info("Starting stuck task cleanup - checking IN_PROGRESS tasks for activity");

        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString());

        if (inProgressTasks.isEmpty()) {
            log.debug("No IN_PROGRESS tasks found");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int failedCount = 0;
        int initializedCount = 0;
        int updatedCount = 0;

        for (Task task : inProgressTasks) {
            LocalDateTime lastUpdate = task.getLastUpdateDate();
            if (lastUpdate == null) {
                lastUpdate = task.getCreationDate();
            }

            LocalDateTime progressLastChanged = task.getProgressLastChangedDate();

            // Truncate to seconds to avoid precision issues when comparing
            lastUpdate = lastUpdate.truncatedTo(ChronoUnit.SECONDS);
            if (progressLastChanged != null) {
                progressLastChanged = progressLastChanged.truncatedTo(ChronoUnit.SECONDS);
            }

            // Case 1: PLCD is null - First scheduler check, initialize and skip
            if (progressLastChanged == null) {
                task.setProgressLastChangedDate(task.getLastUpdateDate());
                taskRepository.save(task);
                initializedCount++;
                log.debug("Task {} - First check, initialized PLCD = LUD", task.getId());
                continue;
            }

            // Case 2: LUD > PLCD - Task has progressed, update and skip
            if (lastUpdate.isAfter(progressLastChanged)) {
                task.setProgressLastChangedDate(task.getLastUpdateDate());
                taskRepository.save(task);
                updatedCount++;
                log.debug("Task {} - Progress detected, updated PLCD = LUD", task.getId());
            }
            // Case 3: LUD == PLCD - Task is stuck, KILL it
            else if (lastUpdate.equals(progressLastChanged) || lastUpdate.isBefore(progressLastChanged)) {
                long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(task.getProgressLastChangedDate(), now);
                log.warn("Task {} (type: {}) is STUCK - LUD == PLCD, no updates for {} minutes",
                        task.getId(), task.getType(), minutesSinceLastUpdate);
                failTask(task, now, minutesSinceLastUpdate);
                failedCount++;
            }
        }

        if (failedCount > 0 || initializedCount > 0 || updatedCount > 0) {
            log.info("Stuck task cleanup completed - {} initialized, {} updated, {} KILLED",
                    initializedCount, updatedCount, failedCount);
        } else {
            log.debug("All IN_PROGRESS tasks are healthy");
        }
    }

    /**
     * Mark a task as FAILED with appropriate error message and details.
     *
     * @param task the task to fail
     * @param now the current timestamp
     * @param minutesWithoutUpdate minutes since last update
     */
    private void failTask(Task task, LocalDateTime now, long minutesWithoutUpdate) {
        try {
            // Create error message
            String errorMessage = String.format(
                "Task has been stuck with no updates for %d minutes and has been automatically terminated.",
                minutesWithoutUpdate
            );
            String failureReason = "no updates for " + minutesWithoutUpdate + " minutes";

            // Update task details - LogUtils.error/info automatically truncate to fit database limit
            List<String> details = task.getDetails() != null ? new ArrayList<>(task.getDetails()) : new ArrayList<>();
            details.add(LogUtils.error(errorMessage));
            details.add(LogUtils.error("Task was automatically failed by the stuck task cleanup scheduler"));

            // Set errors - truncate raw message to fit database limit
            List<String> errors = new ArrayList<>();
            errors.add(LogUtils.truncateToDbLimit(errorMessage));

            // Update task status
            task.setStatus(TaskStatus.FAILED.toString());
            task.setLastUpdateDate(now);
            task.setDetails(details);
            task.setErrors(errors);
            task.setProgressPercentage(task.getProgressPercentage()); // Keep current progress

            taskRepository.save(task);

            log.info("Task {} (type: {}) marked as FAILED due to {}",
                    task.getId(), task.getType(), failureReason);

        } catch (Exception e) {
            log.error("Error while failing stuck task {}: {}", task.getId(), e.getMessage(), e);
        }
    }

    /**
     * Get the configured stuck task timeout in hours.
     *
     * @return timeout in hours
     */
    public double getStuckTaskTimeoutHours() {
        return stuckTaskTimeoutHours;
    }

    /**
     * Check if stuck task cleanup is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isStuckTaskCheckEnabled() {
        return stuckTaskCheckEnabled;
    }
}
