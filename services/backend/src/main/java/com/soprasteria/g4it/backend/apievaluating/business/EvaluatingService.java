/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.AsyncEvaluatingService;
import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
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
    OrganizationService organizationService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    DigitalServiceRepository digitalServiceRepository;

    @Autowired
    @Qualifier("taskExecutorSingleThreaded")
    TaskExecutor taskExecutor;

    @Autowired
    CriteriaService criteriaService;

    @Autowired
    ExportService exportService;

    /**
     * Async Service where is executed the evaluation
     */
    @Autowired
    AsyncEvaluatingService asyncEvaluatingService;

    @Autowired
    OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Autowired
    OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    /**
     * Evaluating an inventory
     *
     * @param subscriber     the subscriber
     * @param organizationId the organization id
     * @param inventoryId    the inventory id
     * @return the Task created
     */
    public Task evaluating(final String subscriber,
                           final Long organizationId,
                           final Long inventoryId) {

        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow();

        manageTasks(subscriber, organizationId, inventory);

        Context context = Context.builder()
                .subscriber(subscriber)
                .organizationId(organizationId)
                .organizationName(organizationService.getOrganizationById(organizationId).getName())
                .inventoryId(inventoryId)
                .locale(LocaleContextHolder.getLocale())
                .datetime(LocalDateTime.now())
                .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                .hasApplications(inventory.getApplicationCount() > 0)
                .build();

        List<String> activeCriteria = criteriaService.getSelectedCriteriaForInventory(subscriber, organizationId, inventory.getCriteria())
                .active();

        // evaluate impacts on 5 default criteria if no activeCriteria
        List<String> criteriaToSet = Optional.ofNullable(activeCriteria)
                .filter(criteria -> !criteria.isEmpty())
                .orElseGet(() -> Constants.CRITERIA_LIST.subList(0, 5));

        // create task with type LOADING
        Task task = Task.builder()
                .creationDate(context.getDatetime())
                .details(new ArrayList<>())
                .lastUpdateDate(context.getDatetime())
                .progressPercentage("0%")
                .status(TaskStatus.TO_START.toString())
                .type(TaskType.EVALUATING.toString())
                .inventory(inventory)
                .criteria(criteriaToSet)
                .createdBy(inventory.getCreatedBy())
                .build();

        taskRepository.save(task);

        // run loading async task
        taskExecutor.execute(new BackgroundTask(context, task, asyncEvaluatingService));

        return task;
    }

    /**
     * Evaluating an inventory
     *
     * @param subscriber        the subscriber
     * @param organizationId    the organization id
     * @param digitalServiceUid digitalServiceUid
     * @return the Task created
     */
    public Task evaluatingDigitalService(final String subscriber,
                                         final Long organizationId,
                                         final String digitalServiceUid) {

        DigitalService digitalService = digitalServiceRepository.findById(digitalServiceUid)
                .orElseThrow(() -> new G4itRestException("404", String.format("Digital Service %s not found.", digitalServiceUid)));

        Context context = Context.builder()
                .subscriber(subscriber)
                .organizationId(organizationId)
                .organizationName(organizationService.getOrganizationById(organizationId).getName())
                .digitalServiceUid(digitalServiceUid)
                .digitalServiceName(digitalService.getName())
                .locale(LocaleContextHolder.getLocale())
                .datetime(LocalDateTime.now())
                .hasVirtualEquipments(true)
                .hasApplications(false)
                .build();

        List<String> activeCriteria = criteriaService.getSelectedCriteriaForDigitalService(subscriber, organizationId, digitalService.getCriteria())
                .active();

        // evaluate impacts on 5 default criteria if no activeCriteria
        List<String> criteriaToSet = Optional.ofNullable(activeCriteria)
                .filter(criteria -> !criteria.isEmpty())
                .orElseGet(() -> Constants.CRITERIA_LIST.subList(0, 5));

        // create task with type EVALUATING_DIGITAL_SERVICE
        Task task = taskRepository.findByDigitalServiceUid(digitalService.getUid())
                .orElseGet(() -> Task.builder()
                        .digitalServiceUid(digitalService.getUid())
                        .type(TaskType.EVALUATING_DIGITAL_SERVICE.toString())
                        .createdBy(digitalService.getUser())
                        .build());

        if (task.getCreationDate() != null) {
            outPhysicalEquipmentRepository.deleteByTaskId(task.getId());
            outVirtualEquipmentRepository.deleteByTaskId(task.getId());
            exportService.cleanExport(task.getId(), subscriber, String.valueOf(organizationId));
        }

        task.setProgressPercentage("0%");
        task.setStatus(TaskStatus.IN_PROGRESS.toString());
        task.setCreationDate(context.getDatetime());
        task.setLastUpdateDate(context.getDatetime());
        task.setCriteria(criteriaToSet);
        task.setDetails(new ArrayList<>());

        taskRepository.save(task);

        // run loading async task
        asyncEvaluatingService.execute(context, task);

        digitalService.setLastCalculationDate(LocalDateTime.now());
        digitalServiceRepository.save(digitalService);

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
                .filter(task -> task.getLastUpdateDate().plusMinutes(15).isBefore(now) && task.getInventory() != null)
                .forEach(task -> {
                    task.setStatus(TaskStatus.TO_START.toString());
                    task.setLastUpdateDate(now);
                    task.setDetails(new ArrayList<>());
                    task.setProgressPercentage("0%");
                    taskRepository.save(task);

                    final Inventory inventory = task.getInventory();
                    final Organization organization = inventory.getOrganization();
                    final String subscriber = organization.getSubscriber().getName();
                    manageTasks(subscriber, organization.getId(), inventory);

                    final Context context = Context.builder()
                            .subscriber(subscriber)
                            .organizationId(organization.getId())
                            .organizationName(organization.getName())
                            .inventoryId(task.getInventory().getId())
                            .locale(LocaleContextHolder.getLocale())
                            .datetime(now)
                            .hasVirtualEquipments(inventory.getVirtualEquipmentCount() > 0)
                            .hasApplications(inventory.getApplicationCount() > 0)
                            .build();

                    log.warn("Restart task {} with taskId={}", TaskType.EVALUATING, task.getId());

                    taskExecutor.execute(new BackgroundTask(context, task, asyncEvaluatingService));
                });
    }

    /**
     * Manage tasks:
     * - check for already running task
     * - clean old tasks, always keep the 2 last tasks
     *
     * @param subscriber     the subscriber
     * @param organizationId the organization id
     * @param inventory      the inventory
     */
    private void manageTasks(String subscriber, Long organizationId, Inventory inventory) {
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
                    exportService.cleanExport(task.getId(), subscriber, String.valueOf(organizationId));
                });
    }


}
