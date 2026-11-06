/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import com.soprasteria.g4it.backend.apireferential.repository.ItemTypeRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Inventory Service.
 */
@Service
@NoArgsConstructor
public class InventoryIndicatorService {

    @Autowired
    ExportService exportService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private WorkspaceService workspaceService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private InDatacenterRepository inDatacenterRepository;
    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    private InApplicationRepository inApplicationRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ItemTypeRepository itemTypeRepository;

    /**
     * Get last batch name in the inventory business object.
     *
     * @param inventory the inventory business object.
     * @return the last batch name or else throw exception.
     */
    private Long getLastTaskId(final InventoryBO inventory) {
        Task task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder()
                        .id(inventory.getId())
                        .build())
                .orElseThrow(() -> new G4itRestException("404", String.format("inventory %d has no batch executed", inventory.getId())));
        return task.getId();

    }

    /**
     * Get inventory indicators.
     * *
     * * @param organization    the organization.
     * * @param workspaceId  the workspaceId.
     * * @param inventoryId the inventory id.
     * * @return indicators.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final String organization, final Long workspaceId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        Map<String, EquipmentIndicatorBO> indicators =
                indicatorService.getEquipmentIndicators(getLastTaskId(inventory));
        // Keep applicable definitions ordered so workspace and organization overrides win.
        Map<String, String> equipmentCategories = itemTypeRepository
                .findByWorkspaceIdOrWorkspaceIdIsNull(workspaceId).stream()
                .filter(itemType -> itemType.getOrganization() == null
                        || Objects.equals(itemType.getOrganization(), organization))
                .filter(itemType -> itemType.getItemCategory() != null)
                .sorted(Comparator
                        .comparing((ItemType itemType) -> !Objects.equals(itemType.getWorkspaceId(), workspaceId))
                        .thenComparing(itemType -> !Objects.equals(itemType.getOrganization(), organization)))
                .collect(Collectors.toMap(
                        ItemType::getType,
                        ItemType::getItemCategory,
                        (preferred, ignored) -> preferred
                ));
        // Items without a matching definition default to infrastructure, but they all should have a definition.
        indicators.values().stream()
                .flatMap(indicator -> indicator.getImpacts().stream())
                .forEach(impact -> impact.setItemCategory(
                        equipmentCategories.getOrDefault(impact.getEquipment(), "INFRASTRUCTURE")));
        return indicators;
    }

    /**
     * Get inventory application indicators.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @return indicators.
     */

    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final String organization, final Long workspaceId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getApplicationIndicators(getLastTaskId(inventory));
    }

    /**
     * Delete inventory indicators.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     */
    public void deleteIndicators(final String organization, final Long workspaceId, final Long inventoryId) {
        // clean all evaluating tasks
        taskService.deleteEvaluatingTasksByInventoryId(organization, workspaceId, inventoryId);
    }

    /**
     * Get datacenter indicators.
     *
     * @param inventoryId the inventory id.
     * @return datacenter indicators
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final Long inventoryId) {
        return indicatorService.getDataCenterIndicators(inventoryId);
    }

    /**
     * Get physical equipment average age indicators.
     *
     * @param inventoryId the inventory id.
     * @return datacenter indicators
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentAvgAge(inventoryId);
    }

    /**
     * Get physical equipment low impact indicators.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @return indicators
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String organization, final Long workspaceId, final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId);
    }

    /**
     * Get electric consumption of physical equipments
     *
     * @param organization the organization
     * @param workspaceId  the workspace id
     * @param inventoryId  the inventory id
     * @return electric consumption indicators
     */
    public List<PhysicalEquipmentElecConsumptionBO> getPhysicalEquipmentElecConsumption(final String organization, final Long workspaceId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        Long taskId = getLastTaskId(inventory);
        final Long criteriaNumber = inventoryService.getCriteriaNumber(taskId);
        return indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber);
    }

    public List<VirtualEquipmentLowImpactBO> getVirtualEquipmentsLowImpact(final String organization, final Long workspaceId, final Long inventoryId) {
        return indicatorService.getVirtualEquipmentsLowImpact(organization, workspaceId, inventoryId);
    }

    public List<VirtualEquipmentElecConsumptionBO> getVirtualEquipmentElecConsumption(final String organization, final Long workspace, final Long inventoryId) {

        Task task = taskRepository
                .findByInventoryAndLastCreationDate(
                        Inventory.builder().id(inventoryId).build())
                .orElseThrow();

        return indicatorService
                .getVirtualEquipmentElecConsumption(
                        task.getId()
                );
    }
}
