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
    private ExportService exportService;

    /**
     * Execute the Task of type EVALUATING
     *
     * @param task the task
     */
    public void execute(final Context context, final Task task) {

        final Long taskId = task.getId();

        log.info("Start evaluating for {}/{}", context.log(), taskId);

        long start = System.currentTimeMillis();

        final List<String> details = new ArrayList<>();
        details.add(LogUtils.info("Start task"));

        task.setDetails(details);
        task.setStatus(TaskStatus.IN_PROGRESS.toString());
        taskRepository.save(task);

        try {
            Path exportDirectory = exportService.createExportDirectory(taskId);
            evaluateService.doEvaluate(context, task, exportDirectory);
            exportService.uploadExportZip(taskId, context.getSubscriber(), context.getOrganizationId().toString());
            exportService.clean(taskId);

            task.setStatus(TaskStatus.COMPLETED.toString());
            task.setProgressPercentage("100%");

        } catch (AsyncTaskException e) {
            log.error("Async task with id '{}' failed for '{}' with error: ", taskId, context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error(e.getMessage()));
        } finally {
            task.setDetails(details);
        }

        taskRepository.save(task);

        long end = System.currentTimeMillis();
        log.info("End load input files for {}/{}. Time taken: {}s", context.log(), taskId, (end - start) / 1000);
    }

}
