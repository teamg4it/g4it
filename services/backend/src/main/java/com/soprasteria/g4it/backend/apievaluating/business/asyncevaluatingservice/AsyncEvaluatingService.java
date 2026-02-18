/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.ITaskExecute;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.LogUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        try {
            Path exportDirectory = exportService.createExportDirectory(taskId);

            if (context.isAi()) {
                evaluateAiService.doEvaluateAi(context, task, exportDirectory);
            } else {
                evaluateService.doEvaluate(context, task, exportDirectory);
            }
            exportService.uploadExportZip(taskId, context.getOrganization(), context.getWorkspaceId().toString());
            exportService.clean(taskId);

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
