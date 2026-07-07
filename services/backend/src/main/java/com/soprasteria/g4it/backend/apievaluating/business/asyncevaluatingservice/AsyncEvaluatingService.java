/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.business.TaskTimeoutMonitor;
import com.soprasteria.g4it.backend.common.task.model.ITaskExecute;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.LogUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import com.soprasteria.g4it.backend.exception.TaskTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AsyncEvaluatingService implements ITaskExecute {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    EvaluateService evaluateService;

    @Autowired
    EvaluateAiService evaluateAiService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private TaskTimeoutMonitor taskTimeoutMonitor;

    @Autowired
    private MessageSource messageSource;

    /**
     * Execute the Task of type EVALUATING
     *
     * @param task the task
     */
    public void execute(final Context context, Task task) {

        final Long taskId = task.getId();
        task = taskRepository.findById(taskId).orElseThrow();


        log.info("Start evaluating for {}/{}", context.log(), taskId);

        long start = System.currentTimeMillis();

        final List<String> details = new ArrayList<>();
        details.add(LogUtils.info("Start task"));
        taskRepository.updateTaskState(
                taskId,
                TaskStatus.IN_PROGRESS.toString(),
                java.time.LocalDateTime.now(),
                "0%"
        );
        String finalStatus = TaskStatus.COMPLETED.toString();
        String finalProgress = "100%";
        final List<String> errors = new ArrayList<>();

        try {
            // Check timeout at the beginning
            taskTimeoutMonitor.checkTaskTimeout(taskId);

            Path exportDirectory = exportService.createExportDirectory(taskId);

            // Check timeout before evaluation
            taskTimeoutMonitor.checkTaskTimeout(taskId);

            if (context.isAi()) {
                evaluateAiService.doEvaluateAi(context, task, exportDirectory);
            } else {
                evaluateService.doEvaluate(context, task, exportDirectory);
            }

            // Check timeout after evaluation, before export
            taskTimeoutMonitor.checkTaskTimeout(taskId);

            exportService.uploadExportZip(taskId, context.getOrganization(), context.getWorkspaceId().toString());

            // Check timeout after export
            taskTimeoutMonitor.checkTaskTimeout(taskId);

            exportService.clean(taskId);

        } catch (TaskTimeoutException e) {
            log.error("Task with id '{}' timed out for '{}' - {}", taskId, context.log(), e.getMessage());
            finalStatus = TaskStatus.FAILED.toString();
            finalProgress = "0%";

            // Get the appropriate localized message with fallback
            try {
                Locale locale = context.getLocale() != null ? context.getLocale() : Locale.ENGLISH;
                String localizedMessage = messageSource.getMessage("import.timeout", null, locale);
                details.add(LogUtils.error(localizedMessage));
                errors.add(LogUtils.truncateToDbLimit(localizedMessage));
                log.info("Added timeout error message to task {}: {}", taskId, localizedMessage);
            } catch (Exception msgEx) {
                // Fallback if message retrieval fails
                log.warn("Failed to get localized message for timeout: {}", msgEx.getMessage());
                String fallbackMessage = "Evaluation process timed out. Please try again.";
                details.add(LogUtils.error(fallbackMessage));
                errors.add(LogUtils.truncateToDbLimit(fallbackMessage));
            }
        } catch (AsyncTaskException e) {
            log.error("Async task with id '{}' failed for '{}' with error: ", taskId, context.log(), e);
            finalStatus = TaskStatus.FAILED.toString();
            finalProgress = "0%";
            details.add(LogUtils.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            finalStatus = TaskStatus.FAILED.toString();
            finalProgress = "0%";
            details.add(LogUtils.error(e.getMessage()));
        } catch (IOException e) {
            log.error("IO error for task with id '{}' failed for '{}': ", taskId, context.log(), e);
            finalStatus = TaskStatus.FAILED.toString();
            finalProgress = "0%";
            details.add(LogUtils.error(e.getMessage()));
        } finally {
            task.setDetails(details);
            if (!errors.isEmpty()) {
                task.setErrors(errors);
            }
        }

        taskRepository.updateTaskFinalState(
                taskId,
                finalStatus,
                finalProgress,
                details
        );

        long end = System.currentTimeMillis();
        log.info("End evaluating for {}/{}. Time taken: {}s {}ms", context.log(), taskId, (end - start) / 1000, (end - start) % 1000);
    }

}
