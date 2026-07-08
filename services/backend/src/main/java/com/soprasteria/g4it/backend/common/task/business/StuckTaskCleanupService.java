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

    @Value("${g4it.task.stuck.timeout.hours:0.167}")
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
     * A task is considered stuck if:
     * - Status is IN_PROGRESS
     * - Last update was more than configured hours ago
     *
     * Stuck tasks are marked as FAILED with an appropriate error message.
     */
    @Transactional
    public void failStuckTasks() {
        if (!stuckTaskCheckEnabled) {
            log.debug("Stuck task check is disabled");
            return;
        }

        log.info("Starting stuck task cleanup - checking for tasks stuck for more than {} hours", stuckTaskTimeoutHours);

        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS.toString());

        if (inProgressTasks.isEmpty()) {
            log.debug("No IN_PROGRESS tasks found");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long timeoutMinutes = (long) (stuckTaskTimeoutHours * 60);
        LocalDateTime cutoffTime = now.minusMinutes(timeoutMinutes);

        int failedCount = 0;

        for (Task task : inProgressTasks) {
            if (isTaskStuck(task, cutoffTime)) {
                failTask(task, now);
                failedCount++;
            }
        }

        if (failedCount > 0) {
            log.info("Stuck task cleanup completed - {} task(s) marked as FAILED", failedCount);
        } else {
            log.debug("No stuck tasks found");
        }
    }

    /**
     * Check if a task is stuck based on last update time.
     *
     * @param task the task to check
     * @param cutoffTime the cutoff time before which tasks are considered stuck
     * @return true if task is stuck, false otherwise
     */
    private boolean isTaskStuck(Task task, LocalDateTime cutoffTime) {
        LocalDateTime lastUpdate = task.getLastUpdateDate();

        // If no last update date, use creation date
        if (lastUpdate == null) {
            lastUpdate = task.getCreationDate();
        }

        // Task is stuck if last update is before cutoff time
        boolean isStuck = lastUpdate.isBefore(cutoffTime);

        if (isStuck) {
            long hoursSinceUpdate = ChronoUnit.HOURS.between(lastUpdate, LocalDateTime.now());
            log.warn("Task {} (type: {}) is stuck - last updated {} hours ago",
                    task.getId(), task.getType(), hoursSinceUpdate);
        }

        return isStuck;
    }

    /**
     * Mark a task as FAILED with appropriate error message and details.
     *
     * @param task the task to fail
     * @param now the current timestamp
     */
    private void failTask(Task task, LocalDateTime now) {
        try {
            // Calculate how long the task has been stuck
            LocalDateTime lastUpdate = task.getLastUpdateDate() != null ?
                    task.getLastUpdateDate() : task.getCreationDate();
            long hoursStuck = ChronoUnit.HOURS.between(lastUpdate, now);

            // Get localized error message
            Locale locale = Locale.getDefault(); // Could be enhanced to use user's locale if stored
            String errorMessage = messageSource.getMessage("task.stuck.timeout",
                    new Object[]{hoursStuck},
                    "Task has been stuck for " + hoursStuck + " hours and has been automatically terminated.",
                    locale);

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

            log.info("Task {} (type: {}) marked as FAILED after being stuck for {} hours",
                    task.getId(), task.getType(), hoursStuck);

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
