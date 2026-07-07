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
import com.soprasteria.g4it.backend.exception.TaskTimeoutException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service to monitor and enforce timeout on long-running tasks.
 */
@Service
@Slf4j
public class TaskTimeoutMonitor {

    /**
     * -- GETTER --
     *  Get the configured timeout in minutes.
     *
     * @return timeout in minutes
     */
    @Getter
    @Value("${g4it.import.timeout.minutes:1}")
    private int timeoutMinutes;

    /**
     * -- GETTER --
     *  Get the configured progress check interval in seconds.
     *
     */
    @Getter
    @Value("${g4it.import.progress.check.interval.seconds:30}")
    private int progressCheckIntervalSeconds;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Check if a task has exceeded the timeout period without progress.
     * A task is considered stuck if:
     * - It's been running for more than the configured timeout
     * - OR it hasn't been updated for more than the progress check interval
     *
     * @param taskId the task ID to check
     * @throws TaskTimeoutException if the task has timed out
     */
    public void checkTaskTimeout(Long taskId) {
        log.info("Checking task timeout ");
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || !TaskStatus.IN_PROGRESS.toString().equals(task.getStatus())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime creationDate = task.getCreationDate();
        LocalDateTime lastUpdateDate = task.getLastUpdateDate();

        // Fallback to creation date if task has never been updated
        if (lastUpdateDate == null) {
            lastUpdateDate = creationDate;
        }

        // Calculate elapsed time since creation
        long elapsedMinutes = ChronoUnit.MINUTES.between(creationDate, now);

        // Calculate time since last progress update
        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(lastUpdateDate, now);

        // Check if task exceeded total timeout
        if (elapsedMinutes > timeoutMinutes) {
            log.error("Task {} has exceeded maximum timeout of {} minutes (elapsed: {} minutes)",
                    taskId, timeoutMinutes, elapsedMinutes);
            throw new TaskTimeoutException(
                    String.format("Import process timed out after %d minutes", elapsedMinutes));
        }

        // Check if task is stuck (no progress for configured interval)
        long progressCheckMinutes = Math.max(1, progressCheckIntervalSeconds / 60);
        if (minutesSinceLastUpdate > progressCheckMinutes * 2) {
            log.warn("Task {} appears to be stuck - no progress for {} minutes",
                    taskId, minutesSinceLastUpdate);
        }
    }

}