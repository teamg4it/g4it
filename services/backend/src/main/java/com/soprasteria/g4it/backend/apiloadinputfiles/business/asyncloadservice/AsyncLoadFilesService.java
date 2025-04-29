/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;


import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata.CheckMetadataInventoryFileService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.AsyncLoadMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.util.FileLoadingUtils;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.task.model.ITaskExecute;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.LogUtils;
import com.soprasteria.g4it.backend.exception.AsyncTaskException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AsyncLoadFilesService implements ITaskExecute {

    public static final String TOO_MANY_ERRORS_MESSAGE = "Too many errors in the file ";
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private LoadFileService loadFileService;
    @Autowired
    private AsyncLoadMetadataService asyncLoadMetadataService;
    @Autowired
    private CheckMetadataInventoryFileService checkMetadataInventoryFileService;
    @Autowired
    private FileLoadingUtils fileLoadingUtils;

    /**
     * Execute the Task of type LOADING
     *
     * @param task the task
     */
    public void execute(final Context context, final Task task) {

        log.info("Start load input files for {}", context.log());

        long start = System.currentTimeMillis();

        final List<String> details = new ArrayList<>();
        details.add(LogUtils.info("Start task"));

        task.setDetails(details);
        task.setStatus(TaskStatus.IN_PROGRESS.toString());
        taskRepository.save(task);
        final List<String> errors = new ArrayList<>();
        List<String> filenames = task.getFilenames();
        context.initFileToLoad(fileLoadingUtils.mapFileToLoad(filenames));
        context.initTaskId(task.getId());

        try {
            //Download all files
            fileLoadingUtils.downloadAllFileToLoad(context);

            //Convert all files
            fileLoadingUtils.convertAllFileToLoad(context);

            // Task fails if mandatory headers are missing
            List<String> mandatoryHeaderErrors = loadFileService.mandatoryHeadersCheck(context);
            if (mandatoryHeaderErrors != null && !mandatoryHeaderErrors.isEmpty()) {
                task.setErrors(mandatoryHeaderErrors);
                task.setStatus(TaskStatus.FAILED.toString());
                details.addAll(mandatoryHeaderErrors.stream().map(LogUtils::error).toList());
                details.add(LogUtils.info("Task failed"));
                task.setDetails(details);
                taskRepository.save(task);
                log.error("Task with id '{}' failed due to missing mandatory headers: {}", task.getId(), mandatoryHeaderErrors);
                return;
            }

            //Load Metadata files
            asyncLoadMetadataService.loadInventoryMetadata(context);

            Map<String, Map<Integer, List<LineError>>> coherenceErrors = checkMetadataInventoryFileService.checkMetadataInventoryFile(task.getId(), context.getInventoryId());

            //  Check if any file is exceeding the error threshold before processing any files.
            for (FileToLoad fileToLoad : context.getFilesToLoad()) {
                Map<Integer, List<LineError>> specificFileError = coherenceErrors.getOrDefault(fileToLoad.getFilename(), Map.of());
                long errorNumberInFile = specificFileError.entrySet().stream().flatMap(entry -> entry.getValue().stream()).count();
                if (errorNumberInFile > 50000) {
                    errors.add(LogUtils.error(TOO_MANY_ERRORS_MESSAGE + fileToLoad.getOriginalFileName() + " : " + errorNumberInFile));
                    log.error("Task with id '{}' failed due to too many errors in the file '{}' for '{}'", task.getId(), fileToLoad.getOriginalFileName(), context.log());
                    task.setStatus(TaskStatus.FAILED.toString());
                    details.add(TOO_MANY_ERRORS_MESSAGE + fileToLoad.getOriginalFileName() + " : " + errorNumberInFile);
                    task.setErrors(errors);
                    task.setDetails(details);
                    taskRepository.save(task);

                    long end = System.currentTimeMillis();
                    log.info("End load input files for {}. Time taken: {}s", context.log(), (end - start) / 1000);
                    return;
                }
            }

            int fileNumber = 0;
            for (FileType fileType : List.of(FileType.DATACENTER, FileType.EQUIPEMENT_PHYSIQUE, FileType.EQUIPEMENT_VIRTUEL, FileType.APPLICATION)) {
                for (FileToLoad fileToLoad : context.getFilesToLoad()) {
                    if (fileType.equals(fileToLoad.getFileType())) {

                        Map<Integer, List<LineError>> specificFileError = coherenceErrors.getOrDefault(fileToLoad.getFilename(), Map.of());
                        fileToLoad.setCoherenceErrorByLineNumer(specificFileError);

                        long errorNumberInFile = specificFileError.entrySet().stream().flatMap(entry -> entry.getValue().stream()).count();

                        details.add(LogUtils.info("Manage file " + fileToLoad.getOriginalFileName()));

                        if (errorNumberInFile > 50000) {
                            errors.add(LogUtils.error(TOO_MANY_ERRORS_MESSAGE + fileToLoad.getOriginalFileName() + " : " + errorNumberInFile));
                        } else {
                            errors.addAll(loadFileService.manageFile(context, fileToLoad));
                        }

                        fileNumber++;

                        task.setProgressPercentage(fileNumber * 100 / task.getFilenames().size() + "%");
                        task.setLastUpdateDate(LocalDateTime.now());
                        taskRepository.save(task);

                    }
                }
            }

            boolean hasRejectedFile = fileLoadingUtils.handelRejectedFiles(context.getSubscriber(), context.getOrganizationId(), task.getInventory().getId(), task.getId(), filenames);

            fileLoadingUtils.cleanConvertedFiles(context);

            details.add(LogUtils.info("Finished task successfully"));

            task.setStatus(hasRejectedFile ? TaskStatus.COMPLETED_WITH_ERRORS.toString() : TaskStatus.COMPLETED.toString());
            task.setProgressPercentage("100%");

        } catch (AsyncTaskException e) {
            log.error("Async task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error(e.getMessage()));
        } finally {
            task.setErrors(errors);
            task.setDetails(details);
        }

        taskRepository.save(task);

        loadFileService.setInventoryCounts(context.getInventoryId());

        long end = System.currentTimeMillis();
        log.info("End load input files for {}. Time taken: {}s", context.log(), (end - start) / 1000);
    }


}
