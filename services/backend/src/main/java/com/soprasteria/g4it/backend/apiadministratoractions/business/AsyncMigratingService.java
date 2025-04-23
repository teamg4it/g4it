/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiadministratoractions.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apievaluating.business.EvaluatingService;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.task.model.ITaskExecute;
import com.soprasteria.g4it.backend.common.task.model.TaskStatus;
import com.soprasteria.g4it.backend.common.task.model.TaskType;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AsyncMigratingService implements ITaskExecute {

    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    DigitalServiceRepository digitalServiceRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    InDatacenterRepository inDatacenterRepository;
    @Autowired
    InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    InApplicationRepository inApplicationRepository;
    @Autowired
    EvaluatingService evaluatingService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    private AdministratorActionsInventoryService administratorActionsInventoryService;
    @Autowired
    private AdministratorActionsDigitalServiceService administratorActionsDigitalServiceService;

    @Override
    public void execute(Context context, Task task) {
        log.info("{}, {}", context, task);

        long totalInventory = inventoryRepository.countByIsNewArch(false);
        long totalDigitalService = digitalServiceRepository.countByIsNewArch(false);
        long total = totalInventory + totalDigitalService;

        if (total == 0) {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setProgressPercentage("100%");
            task.setDetails(List.of("0 inventory and digital service to migrate"));
            taskRepository.save(task);
        } else {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            task.setProgressPercentage("1%");
            List<String> details = new ArrayList<>();
            details.add(String.format("Start migrating %d inventories and %d digital services", totalInventory, totalDigitalService));
            task.setDetails(details);
            taskRepository.save(task);

            migrateInventoriesDataToNewFormat(task, total);
            migrateDigitalServicesDataToNewFormat(task, total, totalInventory);

            task.setStatus(TaskStatus.COMPLETED.name());
            task.setProgressPercentage("100%");
            taskRepository.save(task);
        }
        log.info("End migrating to new table");
    }

    /**
     * Migrate inventories data to new format
     */
    public void migrateInventoriesDataToNewFormat(Task task, long total) {
        List<Inventory> inventoriesWithoutNewArch = inventoryRepository.findByIsNewArchFalse();

        for (int i = 0; i < inventoriesWithoutNewArch.size(); i++) {
            Inventory inventory = inventoriesWithoutNewArch.get(i);
            Long inventoryId = inventory.getId();
            Long organizationId = inventory.getOrganization().getId();
            String subscriber = inventory.getOrganization().getSubscriber().getName();

            // Delete new data by inventory id
            inDatacenterRepository.deleteByInventoryId(inventoryId);
            inApplicationRepository.deleteByInventoryId(inventoryId);
            inPhysicalEquipmentRepository.deleteByInventoryId(inventoryId);
            inVirtualEquipmentRepository.deleteByInventoryIdAndInfrastructureType(inventoryId, "NON_CLOUD_SERVERS");

            log.info("Migrating inventory {} into new table", inventoryId);
            administratorActionsInventoryService.migrateInventoriesDataToNewFormat(inventoryId);
            inventory.setIsNewArch(true);
            inventory.setDoExportVerbose(true);
            inventory.setDoExport(true);
            inventory.setIsMigrated(true);
            inventoryRepository.save(inventory);
            migrateIntegrationReport(inventory);
            migrateEvaluationReport(inventory);
            //evaluating
            if (!inPhysicalEquipmentRepository.findByInventoryId(inventoryId).isEmpty()) {
                evaluatingService.evaluating(subscriber, organizationId, inventoryId);
            }

            task.setProgressPercentage(((i + 1) * 100L / total) + "%");
            taskRepository.save(task);
        }
    }

    /**
     * Migrate digital services data to new format
     */
    public void migrateDigitalServicesDataToNewFormat(Task task, long total, long totalInventory) {
        List<DigitalService> digitalServicesWithoutNewArch = digitalServiceRepository.findByIsNewArchFalse();

        for (int i = 0; i < digitalServicesWithoutNewArch.size(); i++) {
            DigitalService digitalService = digitalServicesWithoutNewArch.get(i);

            // Delete new data by digital service uid
            inDatacenterRepository.deleteByDigitalServiceUid(digitalService.getUid());
            inPhysicalEquipmentRepository.deleteByDigitalServiceUid(digitalService.getUid());
            inVirtualEquipmentRepository.deleteByDigitalServiceUidAndInfrastructureType(digitalService.getUid(), "NON_CLOUD_SERVERS");

            administratorActionsDigitalServiceService.migrateDigitalService(digitalService.getUid());

            //evaluating
            String digitalServiceUid = digitalService.getUid();
            Organization organization = organizationRepository.findById(digitalService.getOrganization().getId()).orElseThrow();
            Long organizationId = organization.getId();
            String subscriber = organization.getSubscriber().getName();
            evaluatingService.evaluatingDigitalService(subscriber, organizationId, digitalServiceUid);

            task.setProgressPercentage((((i + 1) + totalInventory) * 100L / total) + "%");
            taskRepository.save(task);
        }
    }

    /**
     * Migrate file loading history from old to new architecture
     */
    private void migrateIntegrationReport(Inventory inventory) {
        List<Task> loadingTasks = inventory.getIntegrationReports().stream()
                .map(integrationReport -> {
                    Task task = new Task();
                    task.setType(TaskType.LOADING.toString());
                    task.setCreationDate(integrationReport.getCreateTime());
                    task.setLastUpdateDate(integrationReport.getEndTime());
                    task.setStatus(integrationReport.getBatchStatusCode());
                    task.setResultFileUrl(integrationReport.getResultFileUrl());
                    task.setResultFileSize(integrationReport.getResultFileSize());
                    task.setProgressPercentage("100%");
                    task.setInventory(inventory);
                    return task;
                })
                .toList();
        List<Task> tasks = taskRepository.findByInventoryAndStatusAndType(inventory, "COMPLETED", TaskType.LOADING.toString());
        if (!tasks.isEmpty()) {
            taskRepository.deleteAll(tasks);
        }
        taskRepository.saveAll(loadingTasks);
    }

    /**
     * Migrate evaluation history from old to new architecture
     */
    private void migrateEvaluationReport(Inventory inventory) {
        List<Task> evaluationTasks = inventory.getEvaluationReports().stream()
                .map(evaluationReport -> {
                    Task task = new Task();
                    task.setType(TaskType.EVALUATING.toString());
                    task.setCreationDate(evaluationReport.getCreateTime());
                    task.setLastUpdateDate(evaluationReport.getEndTime());
                    task.setStatus(evaluationReport.getBatchStatusCode());
                    task.setProgressPercentage(evaluationReport.getProgressPercentage());
                    task.setInventory(inventory);
                    return task;
                })
                .toList();
        taskRepository.saveAll(evaluationTasks);
    }
}
