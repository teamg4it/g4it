/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice;


import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceVersionService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkmetadata.CheckMetadataInventoryFileService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.loadmetadata.AsyncLoadMetadataService;
import com.soprasteria.g4it.backend.apiloadinputfiles.util.FileLoadingUtils;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.FileToLoad;
import com.soprasteria.g4it.backend.common.model.LineError;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private DigitalServiceVersionService digitalServiceVersionService;
    @Autowired
    private AsyncLoadMetadataService asyncLoadMetadataService;
    @Autowired
    private CheckMetadataInventoryFileService checkMetadataInventoryFileService;
    @Autowired
    private FileLoadingUtils fileLoadingUtils;
    @Autowired
    private TaskTimeoutMonitor taskTimeoutMonitor;
    @Autowired
    private MessageSource messageSource;

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

        final boolean isInventory = context.getInventoryId() != null;

        List<String> filenames = task.getFilenames();
        context.initFileToLoad(fileLoadingUtils.mapFileToLoad(filenames, isInventory));
        context.initTaskId(task.getId());

        try {
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            // Check timeout at the beginning

            taskTimeoutMonitor.checkTaskTimeout(task.getId());

            //Download all files
            fileLoadingUtils.downloadAllFileToLoad(context);

            // Check timeout after download
            taskTimeoutMonitor.checkTaskTimeout(task.getId());

            //Convert all files
            fileLoadingUtils.convertAllFileToLoad(context);

            // Check timeout after conversion
            taskTimeoutMonitor.checkTaskTimeout(task.getId());

            // Task fails if mandatory headers are missing
            List<String> mandatoryHeaderErrors = loadFileService.mandatoryHeadersCheck(context);
            if (mandatoryHeaderErrors != null && !mandatoryHeaderErrors.isEmpty()) {
                // Truncate errors to fit database limit
                List<String> truncatedErrors = mandatoryHeaderErrors.stream()
                        .map(LogUtils::truncateToDbLimit)
                        .toList();
                task.setErrors(truncatedErrors);
                task.setStatus(TaskStatus.FAILED.toString());
                details.addAll(mandatoryHeaderErrors.stream().map(LogUtils::error).toList());
                details.add(LogUtils.info("Task failed"));
                task.setDetails(details);
                taskRepository.save(task);
                log.error("Task with id '{}' failed due to missing mandatory headers: {}", task.getId(), mandatoryHeaderErrors);
                return;
            }

            //Load Metadata files
            asyncLoadMetadataService.loadInputMetadata(context);

            // Check timeout after metadata loading
            taskTimeoutMonitor.checkTaskTimeout(task.getId());

            Map<String, Map<Integer, List<LineError>>> coherenceErrors = checkMetadataInventoryFileService.checkMetadataInventoryFile(task.getId(), context.getInventoryId(), context.getDigitalServiceVersionUid());

            // Check timeout after coherence check
            taskTimeoutMonitor.checkTaskTimeout(task.getId());

            //  Check if any file is exceeding the error threshold before processing any files.
            for (FileToLoad fileToLoad : context.getFilesToLoad()) {
                Map<Integer, List<LineError>> specificFileError = coherenceErrors.getOrDefault(fileToLoad.getFilename(), Map.of());
                long errorNumberInFile = specificFileError.entrySet().stream().flatMap(entry -> entry.getValue().stream()).count();
                if (errorNumberInFile > 50000) {
                    errors.add(LogUtils.error(TOO_MANY_ERRORS_MESSAGE + fileToLoad.getOriginalFileName() + " : " + errorNumberInFile));
                    log.error("Task with id '{}' failed due to too many errors in the file '{}' for '{}'", task.getId(), fileToLoad.getOriginalFileName(), context.log());
                    task.setStatus(TaskStatus.FAILED.toString());
                    details.add(LogUtils.error(TOO_MANY_ERRORS_MESSAGE + fileToLoad.getOriginalFileName() + " : " + errorNumberInFile));
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

                        // Check timeout before processing each file
                        taskTimeoutMonitor.checkTaskTimeout(task.getId());

                        Map<Integer, List<LineError>> specificFileError = coherenceErrors.getOrDefault(fileToLoad.getFilename(), Map.of());
                        fileToLoad.setCoherenceErrorByLineNumer(specificFileError);

                        long errorNumberInFile = specificFileError.entrySet().stream().flatMap(entry -> entry.getValue().stream()).count();

                        details.add(LogUtils.info("Manage file " + fileToLoad.getOriginalFileName()));

                        if (errorNumberInFile > 50000) {
                            errors.add(LogUtils.error(TOO_MANY_ERRORS_MESSAGE + fileToLoad.getOriginalFileName() + " : " + errorNumberInFile));
                        } else {
                            // Truncate errors from loadFileService to fit database limit
                            List<String> fileErrors = loadFileService.manageFile(context, fileToLoad);
                            errors.addAll(fileErrors.stream().map(LogUtils::truncateToDbLimit).toList());
                        }

                        fileNumber++;

                        task.setProgressPercentage(fileNumber * 100 / task.getFilenames().size() + "%");
                        task.setLastUpdateDate(LocalDateTime.now());
                        taskRepository.save(task);

                    }
                }
            }


            boolean hasRejectedFile = fileLoadingUtils.handelRejectedFiles(context.getOrganization(), context.getWorkspaceId(),
                    context.getInventoryId(), context.getDigitalServiceVersionUid(), task.getId(), filenames);

            fileLoadingUtils.cleanConvertedFiles(context);

            details.add(LogUtils.info("Finished task successfully"));

            task.setStatus(hasRejectedFile ? TaskStatus.COMPLETED_WITH_ERRORS.toString() : TaskStatus.COMPLETED.toString());
            task.setProgressPercentage("100%");

        } catch (TaskTimeoutException e) {
            log.error("Task with id '{}' timed out for '{}' - {}", task.getId(), context.log(), e.getMessage());
            task.setStatus(TaskStatus.FAILED.toString());
            // Get the appropriate localized message
            Locale locale = context.getLocale() != null ? context.getLocale() : Locale.getDefault();
            String localizedMessage = messageSource.getMessage("import.timeout", null, locale);
            details.add(LogUtils.error(localizedMessage));
            errors.add(LogUtils.truncateToDbLimit(localizedMessage));
        } catch (AsyncTaskException e) {
            log.error("Async task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Task with id '{}' failed for '{}' with error: ", task.getId(), context.log(), e);
            task.setStatus(TaskStatus.FAILED.toString());
            details.add(LogUtils.error(e.getMessage()));
        }   finally {
            task.setErrors(errors);
            task.setDetails(details);
        }

        taskRepository.save(task);
        if (isInventory) {
            loadFileService.linkApplicationsToVirtualEquipments(context.getInventoryId()); // fix app table links
            loadFileService.setInventoryCounts(context.getInventoryId());
        } else {
            digitalServiceVersionService.updateLastUpdateDate(context.getDigitalServiceVersionUid());
        }


        long end = System.currentTimeMillis();
        log.info("End load input files for {}. Time taken: {}s", context.log(), (end - start) / 1000);
    }


}
