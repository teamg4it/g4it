// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

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
import com.soprasteria.g4it.backend.apiinout.modeldb.PhysicalEquipmentEvaluationTrace;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.PhysicalEquipmentEvaluationTraceRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apireferential.business.ItemCategoryResolver;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    private ItemCategoryResolver itemCategoryResolver;
    @Autowired
    private PhysicalEquipmentEvaluationTraceRepository physicalEquipmentEvaluationTraceRepository;

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
        final Long taskId = getLastTaskId(inventory);
        Map<String, EquipmentIndicatorBO> indicators =
                indicatorService.getEquipmentIndicators(taskId);
        ItemCategoryResolver.Lookup categories = itemCategoryResolver.prepare(organization, workspaceId);
        indicators.values().stream()
                .flatMap(indicator -> indicator.getImpacts().stream())
                .forEach(impact -> {
                    ItemCategoryResolver.Category category = categories.resolve(impact.getReference(), impact.getEquipment());
                    impact.setItemType(category.itemType());
                    impact.setItemCategory(category.itemCategory());
                });
        if (!indicators.isEmpty()) {
            UsageShares shares = getUsageShares(taskId);
            indicators.values().forEach(indicator -> {
                indicator.setByodDevicesPercentage(shares.byod());
                indicator.setCopeDevicesPercentage(shares.cope());
            });
        }
        return indicators;
    }

    /**
     * Count each task/source snapshot once, independently of grouped lifecycle/criterion results.
     * Use the shared resolver's exact Device rule on its persisted evaluation-time category,
     * never today's referential or raw usage values. Null quantities follow evaluation aggregation's
     * default of one. Allocation factors must not weight adoption quantities.
     *
     * Numeric percentages remain unrounded, like the existing low-impact percentage value.
     * Its integer display formatter (including positive values below one) remains a presentation concern.
     */
    private UsageShares getUsageShares(Long taskId) {
        double devices = 0d;
        double byod = 0d;
        double cope = 0d;
        int pageNumber = 0;
        Page<PhysicalEquipmentEvaluationTrace> page;
        do {
            page = physicalEquipmentEvaluationTraceRepository.findByTaskId(taskId,
                    PageRequest.of(pageNumber++, Constants.BATCH_SIZE_10000, Sort.by("id")));
            for (PhysicalEquipmentEvaluationTrace trace : page.getContent()) {
                ItemCategoryResolver.Category category =
                        new ItemCategoryResolver.Category(trace.getItemType(), trace.getItemCategory());
                if (!category.isDevice()) {
                    continue;
                }
                double quantity = trace.getOriginalQuantity() == null ? 1d : trace.getOriginalQuantity();
                devices += quantity;
                if ("BYOD".equals(trace.getUsageContext())) {
                    byod += quantity;
                } else if ("COPE".equals(trace.getUsageContext())) {
                    cope += quantity;
                }
            }
        } while (page.hasNext());
        return devices == 0d ? new UsageShares(null, null)
                : new UsageShares(byod / devices * 100d, cope / devices * 100d);
    }

    private record UsageShares(Double byod, Double cope) {
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
     * §4.4 - get a single DB-side page of application indicators for the table view,
     * filtered by the shared dimensions.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @param filters      the shared filter dimensions.
     * @param pageable     the page/size request.
     * @return the requested page.
     */
    public ApplicationIndicatorsPageBO getApplicationIndicatorsPage(final String organization, final Long workspaceId,
                                                                     final Long inventoryId,
                                                                     final ApplicationCriteriaFilterBO filters,
                                                                     final org.springframework.data.domain.Pageable pageable) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getApplicationIndicatorsPage(getLastTaskId(inventory), filters, pageable);
    }

    /**
     * §4.1 - get aggregated impact totals per criterion, filtered by the shared
     * dimensions.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @param filters      the shared filter dimensions.
     * @return one aggregated entry per matching criterion.
     */
    public List<ApplicationMultiCriteriaImpactBO> getApplicationMultiCriteriaImpacts(final String organization,
                                                                                      final Long workspaceId,
                                                                                      final Long inventoryId,
                                                                                      final ApplicationCriteriaFilterBO filters) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getApplicationMultiCriteriaImpacts(getLastTaskId(inventory), filters);
    }

    /**
     * §4.2/§4.3 - get the dual-axis (graphLevel x repartition) aggregation for
     * the graph view, including drill-down navigation.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @param criteria     the criteria to aggregate.
     * @param graphLevel   the current drill/tree position.
     * @param repartition  the fixed secondary breakdown axis.
     * @param filters      the shared + click-path filter dimensions.
     * @return one entry per requested criterion, with nested nodes/repartitions.
     */
    public List<ApplicationMultiCriteriaBO> getApplicationMultiCriteriaIndicators(final String organization,
                                                                                   final Long workspaceId,
                                                                                   final Long inventoryId,
                                                                                   final List<String> criteria,
                                                                                   final GraphLevel graphLevel,
                                                                                   final RepartitionType repartition,
                                                                                   final ApplicationCriteriaFilterBO filters) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getApplicationMultiCriteriaIndicators(getLastTaskId(inventory), criteria, graphLevel, repartition, filters);
    }

    /**
     * Get aggregated total and low-impact virtual equipment counts, filtered by
     * the shared dimensions.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @param filters      the shared filter dimensions.
     * @return the aggregated total and low-impact counts.
     */
    public VirtualEquipmentLowImpactCountBO getVirtualEquipmentLowImpactCount(final String organization,
                                                                               final Long workspaceId,
                                                                               final Long inventoryId,
                                                                               final ApplicationCriteriaFilterBO filters) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getVirtualEquipmentLowImpactCount(getLastTaskId(inventory), inventoryId, workspaceId, filters);
    }

    /**
     * §4.0 - get distinct environment/equipmentType/lifeCycle/domain/subDomain
     * values for the inventory's latest task, used to populate the application
     * view's filter selectors.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @return the distinct filter values.
     */
    public ApplicationFiltersBO getApplicationFilters(final String organization,
                                                       final Long workspaceId,
                                                       final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getApplicationFilters(getLastTaskId(inventory));
    }

    /**
     * Delete inventory indicators.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     */
    @org.springframework.cache.annotation.Caching(evict = {
            @org.springframework.cache.annotation.CacheEvict(value = "applicationMultiCriteriaImpacts", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "applicationMultiCriteria", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "applicationHierarchyCounts", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "applicationFilters", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "virtualEquipmentLowImpactCount", allEntries = true),
            @org.springframework.cache.annotation.CacheEvict(value = "virtualEquipmentElecConsumptionSum", allEntries = true)
    })

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

    /**
     * Get aggregated electricity consumption sum and virtual equipment quantity
     * sum, filtered by the shared dimensions.
     *
     * @param organization the organization.
     * @param workspaceId  the workspaceId.
     * @param inventoryId  the inventory id.
     * @param filters      the shared filter dimensions.
     * @return the aggregated electricity consumption and quantity sums.
     */
    public VirtualEquipmentElecConsumptionSumBO getVirtualEquipmentElecConsumptionSum(final String organization,
                                                                                       final Long workspaceId,
                                                                                       final Long inventoryId,
                                                                                       final ApplicationCriteriaFilterBO filters) {
        final InventoryBO inventory = inventoryService.getInventory(organization, workspaceId, inventoryId);
        return indicatorService.getVirtualEquipmentElecConsumptionSum(getLastTaskId(inventory), filters);
    }
}
