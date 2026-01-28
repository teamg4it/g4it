/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.AsyncEvaluatingService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.BackgroundTask;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EvaluatingService {

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    DigitalServiceVersionRepository digitalServiceVersionRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    @Qualifier("taskExecutorSingleThreaded")
    TaskExecutor taskExecutor;

    @Autowired
    CriteriaService criteriaService;

    @Autowired
    ExportService exportService;
    @Autowired
    AuthService authService;

    /**
     * Async Service where is executed the evaluation
     */
    @Autowired
    AsyncEvaluatingService asyncEvaluatingService;

    /**
     * Evaluating an inventory
     *
     * @param organization the organization
     * @param workspaceId  the workspace id
     * @param inventoryId  the inventory id
     * @return the Task created
     */
    public Task evaluating(final String organization,
                           final Long workspaceId,
                           final Long inventoryId) {

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new G4itRestException("404", "Inventory not found."));


        manageInventoryTasks(organization, workspaceId, inventory);

        Context context = Context.builder()
                .organization(organization)
                .workspaceId(workspaceId)
                .workspaceName(workspaceService.getWorkspaceById(workspaceId).getName())
                .inventoryId(inventoryId)
                .locale(LocaleContextHolder.getLocale())
                .datetime(LocalDateTime.now())
                .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                .hasApplications(inventory.getApplicationCount() > 0)
                .build();

        List<String> activeCriteria = criteriaService.getSelectedCriteriaForInventory(organization, workspaceId, inventory.getCriteria())
                .active();

        // evaluate impacts on 5 default criteria if no activeCriteria
        List<String> criteriaToSet = Optional.ofNullable(activeCriteria)
                .filter(criteria -> !criteria.isEmpty())
                .orElseGet(() -> Constants.CRITERIA_LIST.subList(0, 5));

        User user = userRepository.findById(authService.getUser().getId()).orElseThrow();

        // create task with type EVALUATING
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.EVALUATING.toString())
                .inventory(inventory)
                .criteria(criteriaToSet)
                .createdBy(user)
                .build();

        taskRepository.save(task);
        taskRepository.updateTaskState(
                task.getId(),
                TaskStatus.IN_PROGRESS.toString(),
                LocalDateTime.now(),
                "0%"
        );

        // evaluation may be heavy, so runs in threaded executor to avoid performance issues
        taskExecutor.execute(new BackgroundTask(context, task, asyncEvaluatingService));

        return task;
    }

    /**
     * Evaluating an inventory
     *
     * @param organization             the organization
     * @param workspaceId              the workspace id
     * @param digitalServiceVersionUid digitalServiceUid
     * @return the Task created
     */
    public Task evaluatingDigitalService(final String organization,
                                         final Long workspaceId,
                                         final String digitalServiceVersionUid) {

        DigitalServiceVersion digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid)
                .orElseThrow(() -> new G4itRestException("404", String.format("Digital Service %s not found.", digitalServiceVersionUid)));

        DigitalService digitalService = digitalServiceVersion.getDigitalService();
        manageDigitalServiceTasks(organization, workspaceId, digitalServiceVersion);

        Context context = Context.builder()
                .organization(organization)
                .workspaceId(workspaceId)
                .workspaceName(workspaceService.getWorkspaceById(workspaceId).getName())
                .digitalServiceUid(digitalServiceVersion.getDigitalService().getUid())
                .digitalServiceName(digitalServiceVersion.getDigitalService().getName())
                .digitalServiceVersionUid(digitalServiceVersionUid)
                .digitalServiceVersionName(digitalServiceVersion.getDescription())
                .locale(LocaleContextHolder.getLocale())
                .datetime(LocalDateTime.now())
                .hasVirtualEquipments(true)
                .hasApplications(false)
                .isAi(digitalServiceVersion.getDigitalService().isAi())
                .build();

        List<String> activeCriteria = criteriaService.getSelectedCriteriaForDigitalService(organization, workspaceId, digitalServiceVersion.getCriteria())
                .active();

        // evaluate impacts on 5 default criteria if no activeCriteria
        List<String> criteriaToSet = Optional.ofNullable(activeCriteria)
                .filter(criteria -> !criteria.isEmpty())
                .orElseGet(() -> Constants.CRITERIA_LIST.subList(0, 5));

        User user = userRepository.findById(authService.getUser().getId()).orElseThrow();

        // create task with type EVALUATING_DIGITAL_SERVICE
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.IN_PROGRESS.toString())
                .type(TaskType.EVALUATING_DIGITAL_SERVICE.toString())
                .digitalServiceVersion(digitalServiceVersion)
                .criteria(criteriaToSet)
                .createdBy(user)
                .build();

        taskRepository.save(task);
        taskRepository.updateTaskState(
                task.getId(),
                TaskStatus.IN_PROGRESS.toString(),
                LocalDateTime.now(),
                "0%"
        );

        // evaluation may be heavy, so runs in threaded executor to avoid performance issues
        taskExecutor.execute(new BackgroundTask(context, task, asyncEvaluatingService));

        digitalService.setLastCalculationDate(LocalDateTime.now());
        digitalServiceRepository.save(digitalService);

        digitalServiceVersion.setLastCalculationDate(LocalDateTime.now());
        digitalServiceVersionRepository.save(digitalServiceVersion);
        return task;
    }

    /**
     * Get task with type EVALUATING and IN_PROGRESS and lastUpdateDate > 1 min from now
     * Change the status to TO_START and execute the task in background
     */
    @Transactional
    public void restartEvaluating() {
        List<Task> inProgressLoadingTasks = taskRepository.findByStatusAndType(TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString());

        if (inProgressLoadingTasks.isEmpty()) return;

        final LocalDateTime now = LocalDateTime.now();

        // check tasks to restart
        inProgressLoadingTasks.stream()
                .filter(task -> task.getInventory() != null)
                .filter(task -> task.getLastUpdateDate() == null
                        || task.getLastUpdateDate().plusMinutes(15).isBefore(now))
                .forEach(task -> {
                    task.setStatus(TaskStatus.TO_START.toString());
                    task.setLastUpdateDate(now);
                    task.setDetails(new ArrayList<>());
                    task.setProgressPercentage("0%");
                    taskRepository.updateTaskStateWithDetails(
                            task.getId(),
                            TaskStatus.TO_START.toString(),
                            now,
                            "0%",
                            new ArrayList<>()
                    );

                    final Inventory inventory = task.getInventory();
                    final Workspace workspace = inventory.getWorkspace();
                    final String organization = workspace.getOrganization().getName();
                    try {
                        manageInventoryTasks(organization, workspace.getId(), inventory);
                    } catch (G4itRestException e) {
                        if ("task.already.running".equals(e.getMessage())) {
                            log.info("TaskId={} is already running, so restart is skipped", task.getId());
                            return;
                        }
                        throw e;
                    }
                    final Context context = Context.builder()
                            .organization(organization)
                            .workspaceId(workspace.getId())
                            .workspaceName(workspace.getName())
                            .inventoryId(task.getInventory().getId())
                            .locale(LocaleContextHolder.getLocale())
                            .datetime(now)
                            .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                            .hasApplications(inventory.getApplicationCount() > 0)
                            .build();

                    taskRepository.updateTaskState(
                            task.getId(),
                            TaskStatus.IN_PROGRESS.toString(),
                            now,
                            "0%"
                    );
                    log.warn("Restart task {} with taskId={}", TaskType.EVALUATING, task.getId());

                    taskExecutor.execute(new BackgroundTask(context, task, asyncEvaluatingService));
                });
    }

    /**
     * Manage tasks:
     * - check for already running task
     * - clean old tasks, always keep the 2 last tasks
     *
     * @param organization the organization
     * @param workspaceId  the workspace id
     * @param inventory    the inventory
     */
    private void manageInventoryTasks(String organization, Long workspaceId, Inventory inventory) {
        // check if any task is already running
        List<Task> tasks = taskRepository.findByInventoryAndStatusAndType(inventory, TaskStatus.IN_PROGRESS.toString(), TaskType.EVALUATING.toString());
        if (!tasks.isEmpty()) {
            throw new G4itRestException("409", "task.already.running");
        }

        // clean old tasks
        taskRepository.findByInventoryAndType(inventory, TaskType.EVALUATING.toString())
                .stream()
                .sorted(Comparator.comparing(Task::getId).reversed())
                .skip(2)
                .forEach(task -> {
                    taskRepository.deleteTask(task.getId());
                    exportService.cleanExport(task.getId(), organization, String.valueOf(workspaceId));
                });
    }

    /**
     * Manage tasks:
     * - clean old tasks, always keep the 2 last tasks
     *
     * @param organization          the organization
     * @param workspaceId           the workspace id
     * @param digitalServiceVersion the digitalService
     */
    private void manageDigitalServiceTasks(String organization, Long workspaceId, DigitalServiceVersion digitalServiceVersion) {

        // clean old tasks
        taskRepository.findByDigitalServiceVersionAndType(digitalServiceVersion, TaskType.EVALUATING_DIGITAL_SERVICE.toString())
                .stream()
                .sorted(Comparator.comparing(Task::getId).reversed())
                .skip(2)
                .forEach(task -> {
                    taskRepository.deleteTask(task.getId());
                    exportService.cleanExport(task.getId(), organization, String.valueOf(workspaceId));
                });
    }


}
