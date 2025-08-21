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

        final Map<String, List<OutPhysicalEquipment>> outPhysicalEquipmentMap = outPhysicalEquipmentRepository.findByTaskId(taskId).stream()
                .collect(Collectors.groupingBy(OutPhysicalEquipment::getCriterion));

        return outPhysicalEquipmentMap.entrySet().stream()
                .collect(Collectors.toMap(
                        map -> com.soprasteria.g4it.backend.common.utils.StringUtils.snakeToKebabCase(map.getKey()),
                        map -> equipmentIndicatorMapper.outToDto(map.getValue())
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
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return low impact indicators.
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber,
                                                                             final Long organizationId,
                                                                             final Long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId);
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

}
