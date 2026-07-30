/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.AsyncLoadFilesService;
import com.soprasteria.g4it.backend.apiloadinputfiles.util.FileValidatorUtils;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.StoredFile;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class LoadInputFilesService {

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    DigitalServiceVersionRepository digitalServiceVersionRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    @Qualifier("taskExecutorSingleThreaded")
    TaskExecutor taskExecutor;
    /**
     * Async Service where is executed the file loading
     */
    @Autowired
    AsyncLoadFilesService asyncLoadFilesService;
    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    AuthService authService;

    @Value("${local.working.folder}")
    private String localWorkingFolder;

    /**
     * Load input files for an inventory
     *
     * @param organization         the organization
     * @param workspaceId     the workspaceId id
     * @param inventoryId        the inventory id
     * @param datacenters        the datacenter files
     * @param physicalEquipments the physical equipment files
     * @param virtualEquipments  the virtual equipment files
     * @param applications       the application files
     * @return the Task created
     */
    public Task loadFiles(final String organization,
                          final Long workspaceId,
                          final Long inventoryId,
                          final List<MultipartFile> datacenters,
                          final List<MultipartFile> physicalEquipments,
                          final List<MultipartFile> virtualEquipments,
                          final List<MultipartFile> applications) {
        final Map<FileType, List<MultipartFile>> allFiles =
                new EnumMap<>(FileType.class);

        if (datacenters != null) {
            allFiles.put(FileType.DATACENTER, datacenters);
        }

        if (physicalEquipments != null) {
            allFiles.put(
                    FileType.EQUIPEMENT_PHYSIQUE,
                    physicalEquipments
            );
        }

        if (virtualEquipments != null) {
            allFiles.put(
                    FileType.EQUIPEMENT_VIRTUEL,
                    virtualEquipments
            );
        }

        if (applications != null) {
            allFiles.put(FileType.APPLICATION, applications);
        }

        if (allFiles.isEmpty()) return new Task();

        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow();

        List<Task> tasks = taskRepository.findByInventoryAndStatusAndType(inventory, TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString());
        if (!tasks.isEmpty()) {
            throw new G4itRestException("500", "task.already.running");
        }

        Context context = Context.builder()
                .organization(organization)
                .workspaceId(workspaceId)
                .workspaceName(workspaceService.getWorkspaceById(workspaceId).getName())
                .inventoryId(inventoryId)
                .datetime(LocalDateTime.now())
                .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                .hasApplications(inventory.getApplicationCount() > 0)
                .build();

        /*
         * CRITICAL FIX:
         * Immediately detach MultipartFiles.
         */
        final Map<FileType, List<StoredFile>> storedFiles =
                detachFiles(allFiles, true);

        FileValidatorUtils.validateFiles(storedFiles);

        /*
         * Now work ONLY with StoredFile.
         */
        List<String> filenames=persistRenamedFiles(context,storedFiles);
        User user = getAuthenticatedUser();
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.LOADING.toString())
                .inventory(
                        Inventory.builder()
                                .id(inventoryId)
                                .build()
                )
                .filenames(filenames)
                .createdBy(user)
                .build();
        saveAndLaunchLoadingTask(context,task);
        return task;
    }


    /**
     * @param organization         the organization
     * @param workspaceId     the workspaceId id
     * @param digitalServiceVersionUid  the dig
     * @param datacenters        the datacenter files
     * @param physicalEquipments the physical equipment files
     * @param virtualEquipments  the virtual equipment files
     * @return the Task created
     */
    public Task loadDigitalServiceFiles(final String organization,
                                        final Long workspaceId,
                                        final String digitalServiceVersionUid,
                                        final List<MultipartFile> datacenters,
                                        final List<MultipartFile> physicalEquipments,
                                        final List<MultipartFile> virtualEquipments) {
        final Map<FileType, List<MultipartFile>> allFiles = new EnumMap<>(FileType.class);
        if (datacenters != null) {
            allFiles.put(FileType.DATACENTER, datacenters);
        }

        if (physicalEquipments != null) {
            allFiles.put(
                    FileType.EQUIPEMENT_PHYSIQUE,
                    physicalEquipments
            );
        }

        if (virtualEquipments != null) {
            allFiles.put(
                    FileType.EQUIPEMENT_VIRTUEL,
                    virtualEquipments
            );
        }
        DigitalServiceVersion digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid).orElseThrow();

        if (allFiles.isEmpty()) return new Task();

        List<Task> tasks = taskRepository.findByDigitalServiceVersionAndStatusAndType(digitalServiceVersion, TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString());
        if (!tasks.isEmpty()) {
            throw new G4itRestException("500", "task.already.running");
        }
        Context context = Context.builder()
                .organization(organization)
                .workspaceId(workspaceId)
                .workspaceName(workspaceService.getWorkspaceById(workspaceId).getName())
                .digitalServiceVersionUid(digitalServiceVersionUid)
                .datetime(LocalDateTime.now())
                .build();
        final Map<FileType, List<StoredFile>> storedFiles =
                detachFiles(allFiles, false);

        FileValidatorUtils.validateFiles(storedFiles);

        List<String> filenames=persistRenamedFiles(context,storedFiles);
        User user = getAuthenticatedUser();

        // create task with type LOADING
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.LOADING.toString())
                .digitalServiceVersion(digitalServiceVersion)
                .filenames(filenames)
                .createdBy(user)
                .build();

        saveAndLaunchLoadingTask(context,task);
        return task;
    }

    /**
     * Get task with type LOADING and IN_PROGRESS and lastUpdateDate > 1 min from now
     * Change the status to TO_START and execute the task in background
     */
    @Transactional
    public void restartLoadingFiles() {
        List<Task> inProgressLoadingTasks = taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.LOADING.toString());
        if (inProgressLoadingTasks.isEmpty()) return;
        final LocalDateTime now = LocalDateTime.now();
        // check tasks to restart
        inProgressLoadingTasks.stream()
                .filter(task -> task.getLastUpdateDate().plusMinutes(15).isBefore(now))
                .forEach(task -> {
                    task.setStatus(TaskStatus.TO_START.toString());
                    task.setLastUpdateDate(now);
                    task.setDetails(new ArrayList<>());
                    task.setProgressPercentage("0%");
                    taskRepository.save(task);
                    Context context;
                    final Inventory inventory = task.getInventory();
                    if (inventory != null) {
                        final Workspace workspace = inventory.getWorkspace();
                        context = Context.builder()
                                .organization(workspace.getOrganization().getName())
                                .workspaceId(workspace.getId())
                                .workspaceName(workspace.getName())
                                .inventoryId(task.getInventory().getId())
                                .locale(Locale.getDefault())
                                .datetime(now)
                                .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                                .hasApplications(inventory.getApplicationCount() > 0)
                                .build();
                    } else {
                        DigitalServiceVersion digitalServiceVersion = task.getDigitalServiceVersion();
                        DigitalService digitalService = digitalServiceVersion.getDigitalService();
                        Workspace workspace = digitalService.getWorkspace();
                        context = Context.builder()
                                .organization(workspace.getOrganization().getName())
                                .workspaceId(workspace.getId())
                                .workspaceName(workspace.getName())
                                .digitalServiceVersionUid(task.getDigitalServiceVersion().getUid())
                                .locale(Locale.getDefault())
                                .datetime(now)
                                .build();
                    }

                    log.warn("Restart task {} with taskId={}", TaskType.LOADING, task.getId());
                    taskExecutor.execute(new BackgroundTask(
                            context,
                            task,
                            asyncLoadFilesService)
                    );
                });
    }


    /**
     * Assign new unique file names to input files
     * target names: ${type}_${UUID}.csv where type is the FileType enum
     *
     * @param files the input files
     * @param type  the type
     * @return the new list of file names
     */
    private List<String> newFilenames(List<StoredFile> files, final FileType type) {
        if (files == null) return new ArrayList<>();
        return files.stream()
                .map(file -> {
                    String originalFilename = file.getOriginalFilename();
                    // ensures the original filename can be properly matched with regex later
                    originalFilename = originalFilename == null ? "" : originalFilename.replace("_", "-");
                    String extension = StringUtils.getFilenameExtension(originalFilename);
                    return String.format("%s_%s_%s.%s", type.toString(), originalFilename, UUID.randomUUID(), extension);
                })
                .toList();
    }

    private StoredFile storeMultipartFile(
            MultipartFile multipartFile,
            boolean isInventory) {

        try {
            Path workingDir = isInventory
                    ? Path.of(localWorkingFolder, "input", "inventory")
                    : Path.of(localWorkingFolder, "input", "digital-service");

            String extension = StringUtils.getFilenameExtension(
                    multipartFile.getOriginalFilename()
            );

            Path storedFile = Files.createTempFile(
                    workingDir,
                    "upload-",
                    extension != null ? "." + extension : ".tmp"
            );
            log.info("Storing multipart file {} into {}", multipartFile.getOriginalFilename(),storedFile);

            /*
             * IMPORTANT:
             * Detach immediately from Tomcat temp storage.
             */
            try (InputStream inputStream =
                         multipartFile.getInputStream()) {
                Files.copy(
                        inputStream,
                        storedFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
            return new StoredFile(
                    storedFile,
                    multipartFile.getOriginalFilename(),
                    multipartFile.getContentType()
            );
        } catch (IOException e) {
            log.error("Failed to store multipart file {} : ",multipartFile.getOriginalFilename(),e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to process uploaded file"
            );
        }
    }

    private void cleanupStoredFiles(
            Map<FileType, List<StoredFile>> storedFiles) {
        storedFiles.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(storedFile -> {
                    try {
                        Files.deleteIfExists(storedFile.getPath());
                        log.info("Deleted temp file {}", storedFile.getPath());
                    } catch (IOException e) {
                        log.warn("Failed to delete temp file {}",storedFile.getPath(),e);
                    }
                });
    }
    // Common for inventory and digital service loading, but not perfect for both, so we can refactor later if needed
    private Map<FileType, List<StoredFile>> detachFiles(
            Map<FileType, List<MultipartFile>> allFiles,
            boolean isInventory) {

        Map<FileType, List<StoredFile>> storedFiles = new EnumMap<>(FileType.class);
        for (Map.Entry<FileType, List<MultipartFile>> entry : allFiles.entrySet()) {
            List<StoredFile> detached = entry.getValue()
                    .stream()
                    .map(file -> storeMultipartFile(file, isInventory))
                    .toList();
            storedFiles.put(entry.getKey(), detached);
        }
        return storedFiles;
    }

    // Common for inventory and digital service loading, but not perfect for both, so we can refactor later if needed
    private List<String> persistRenamedFiles(
            Context context,
            Map<FileType, List<StoredFile>> storedFiles) {

        try {
            return Stream.of(
                            FileType.DATACENTER,
                            FileType.EQUIPEMENT_PHYSIQUE,
                            FileType.EQUIPEMENT_VIRTUEL,
                            FileType.APPLICATION )
                    .map(fileType -> {
                        List<StoredFile> files = storedFiles.get(fileType);
                        List<String> typeFileNames = newFilenames(files, fileType);
                        fileSystemService.manageFilesAndRename(
                                context.getOrganization(),
                                context.getWorkspaceId(),
                                files,
                                typeFileNames,
                                context.getInventoryId() != null );
                        return typeFileNames;
                    })
                    .flatMap(Collection::stream)
                    .toList();
        } finally {
            cleanupStoredFiles(storedFiles);
        }
    }

    // Get authenticated user from database to ensure we have all the needed info (like locale) for task execution
    private User getAuthenticatedUser() {
        return userRepository.findById(authService.getUser().getId()).orElseThrow();
    }

    // Common for inventory and digital service loading, but not perfect for both, so we can refactor later if needed
    private void saveAndLaunchLoadingTask(Context context, Task task) {
        taskRepository.save(task);
        taskExecutor.execute(new BackgroundTask(context, task, asyncLoadFilesService));
    }
}
