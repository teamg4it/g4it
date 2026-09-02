/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.ApplicationIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.EquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.OutApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Indicator Service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class IndicatorService {

    @Autowired
    private DataCenterIndicatorService dataCenterIndicatorService;

    @Autowired
    private PhysicalEquipmentIndicatorService physicalEquipmentIndicatorService;

    @Autowired
    private VirtualEquipmentIndicatorService virtualEquipmentIndicatorService;

    @Autowired
    private EquipmentIndicatorMapper equipmentIndicatorMapper;

    @Autowired
    private ApplicationIndicatorMapper applicationIndicatorMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private OutPhysicalEquipmentRepository outPhysicalEquipmentRepository;

    @Autowired
    private OutApplicationRepository outApplicationRepository;

    /**
     * Retrieve equipment indicators.
     *
     * @param taskId the task id.
     * @return indicator by criteria.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final Long taskId) {

        List<Object[]> results = outPhysicalEquipmentRepository.findCriterionAndEquipmentByTaskId(taskId);
        Map<String, List<OutPhysicalEquipment>> grouped = results.stream()
                .collect(Collectors.groupingBy(
                        r -> (String) r[0],
                        Collectors.mapping(r -> (OutPhysicalEquipment) r[1], Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> com.soprasteria.g4it.backend.common.utils.StringUtils.snakeToKebabCase(e.getKey()),
                        e -> equipmentIndicatorMapper.outToDto(e.getValue())
                ));
    }

    /**
     * Retrieve application indicators.
     *
     * @param taskId the task id.
     * @return indicator by criteria.
     */
    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final Long taskId) {
        List<OutApplication> outApplications = outApplicationRepository.findByTaskId(taskId);
        outApplications.forEach(app -> app.setLifecycleStep(LifecycleStepUtils.getReverse(app.getLifecycleStep())));

        return applicationIndicatorMapper.toOutDto(outApplications);
    }

    /**
     * §4.4 - retrieve a single DB-side page (LIMIT/OFFSET) of flattened
     * application indicator rows, for the table view.
     *
     * @param taskId   the task id.
     * @param pageable the page/size request.
     * @return the requested page.
     */
    public ApplicationIndicatorsPageBO getApplicationIndicatorsPage(final Long taskId,
                                                                     final org.springframework.data.domain.Pageable pageable) {
        final org.springframework.data.domain.Page<OutApplication> page = outApplicationRepository.findByTaskId(taskId, pageable);
        page.getContent().forEach(app -> app.setLifecycleStep(LifecycleStepUtils.getReverse(app.getLifecycleStep())));

        return ApplicationIndicatorsPageBO.builder()
                .content(applicationIndicatorMapper.toRowBO(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * §4.1 - retrieve aggregated impact totals per criterion, filtered by the
     * shared dimensions. 100% database-side aggregation - see
     * spec/applicationViewOptimizationFeature/solution.md §4.1.
     *
     * @param taskId  the task id.
     * @param filters the shared filter dimensions.
     * @return one aggregated entry per matching criterion.
     */
    @org.springframework.cache.annotation.Cacheable(value = "applicationMultiCriteriaImpacts", key = "{#taskId, #filters}")
    public List<ApplicationMultiCriteriaImpactBO> getApplicationMultiCriteriaImpacts(final Long taskId,
                                                                                      final ApplicationCriteriaFilterBO filters) {
        return applicationIndicatorMapper.toMultiCriteriaImpactBO(
                outApplicationRepository.aggregateMultiCriteriaImpacts(taskId, filters));
    }

    /**
     * §4.2/§4.3 - retrieve the dual-axis (graphLevel x repartition) aggregation
     * for the graph view, including drill-down navigation. 100% database-side
     * aggregation - see spec/applicationViewOptimizationFeature/solution.md
     * §4.2/§4.3.
     *
     * @param taskId      the task id.
     * @param criteria    the criteria to aggregate.
     * @param graphLevel  the current drill/tree position (primary grouping).
     * @param repartition the fixed secondary breakdown axis.
     * @param filters     the shared + click-path filter dimensions.
     * @return one entry per requested criterion, with nested nodes/repartitions.
     */
    @org.springframework.cache.annotation.Cacheable(value = "applicationMultiCriteria",
            key = "{#taskId, #criteria, #graphLevel, #repartition, #filters}")
    public List<ApplicationMultiCriteriaBO> getApplicationMultiCriteriaIndicators(final Long taskId,
                                                                                   final List<String> criteria,
                                                                                   final GraphLevel graphLevel,
                                                                                   final RepartitionType repartition,
                                                                                   final ApplicationCriteriaFilterBO filters) {
        return applicationIndicatorMapper.toMultiCriteriaBO(
                outApplicationRepository.aggregateMultiCriteria(taskId, criteria, graphLevel, repartition, filters));
    }

    /**
     * §4.3 (optional) - retrieve standing hierarchy counts (header KPIs) for the
     * whole current filter scope.
     *
     * @param taskId  the task id.
     * @param filters the shared filter dimensions.
     * @return the aggregated hierarchy counts.
     */
    @org.springframework.cache.annotation.Cacheable(value = "applicationHierarchyCounts", key = "{#taskId, #filters}")
    public ApplicationHierarchyCountsBO getApplicationHierarchyCounts(final Long taskId,
                                                                       final ApplicationCriteriaFilterBO filters) {
        return applicationIndicatorMapper.toHierarchyCountsBO(outApplicationRepository.countHierarchy(taskId, filters));
    }

    /**
     * Retrieve datacenter indicators.
     *
     * @param inventoryId the inventory id.
     * @return datacenter indicators.
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final Long inventoryId) {
        return dataCenterIndicatorService.getDataCenterIndicators(inventoryId);
    }

    /**
     * Retrieve average age indicators.
     *
     * @param inventoryId the inventory id.
     * @return average age indicators.
     */

    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentAvgAge(inventoryId);
    }

    /**
     * Retrieve low impact indicators.
     *
     * @param organization the organization.
     * @param workspaceId  the workspace id.
     * @param inventoryId  the inventory id.
     * @return low impact indicators.
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String organization,
                                                                             final Long workspaceId,
                                                                             final Long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentsLowImpact(organization, workspaceId, inventoryId);
    }

    /**
     * Retrieve electric consumption of physical equipments
     *
     * @param taskId the task id
     * @return electric consumption indicators
     */
    public List<PhysicalEquipmentElecConsumptionBO> getPhysicalEquipmentElecConsumption(final Long taskId,
                                                                                        final Long criteriaNumber) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber);
    }

    public List<VirtualEquipmentLowImpactBO> getVirtualEquipmentsLowImpact(final String organization,
                                                                           final Long workspaceId,
                                                                           final Long inventoryId) {

        return virtualEquipmentIndicatorService.getVirtualEquipmentsLowImpact(organization, workspaceId, inventoryId);
    }

    public List<VirtualEquipmentElecConsumptionBO> getVirtualEquipmentElecConsumption(final Long taskId) {
        return virtualEquipmentIndicatorService.getVirtualEquipmentElecConsumption(taskId);
    }

}
