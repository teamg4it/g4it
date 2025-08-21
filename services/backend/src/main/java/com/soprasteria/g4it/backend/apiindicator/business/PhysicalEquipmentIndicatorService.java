/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.PhysicalEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.InPhysicalEquipmentAvgAgeViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InPhysicalEquipmentElecConsumptionViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InPhysicalEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Physical Equipment indicator Service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class PhysicalEquipmentIndicatorService {

    @Autowired
    private InPhysicalEquipmentAvgAgeViewRepository inPhysicalEquipmentAvgAgeViewRepository;

    @Autowired
    private InPhysicalEquipmentLowImpactViewRepository inPhysicalEquipmentLowImpactViewRepository;

    @Autowired
    private InPhysicalEquipmentElecConsumptionViewRepository inPhysicalEquipmentElecConsumptionViewRepository;

    /**
     * Physical equipment indicators mapper.
     */
    @Autowired
    private PhysicalEquipmentIndicatorMapper physicalEquipmentIndicatorMapper;

    /**
     * The Organization Service
     */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * The LowImpact Service
     */
    @Autowired
    private LowImpactService lowImpactService;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Retrieve average age indicators.
     *
     * @param inventoryId the inventory id.
     * @return average age indicators.
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final Long inventoryId) {
        Task task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder().id(inventoryId).build()).orElseThrow();
        var result = inPhysicalEquipmentAvgAgeViewRepository.findPhysicalEquipmentAvgAgeIndicators(task.getId());

        return physicalEquipmentIndicatorMapper.inPhysicalEquipmentAvgAgetoDto(result);
    }


    /**
     * Retrieve low impact indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return low impact indicators.
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber, final Long organizationId, final Long inventoryId) {

        final Workspace linkedWorkspace = workspaceService.getOrganizationById(organizationId);

        final List<InPhysicalEquipmentLowImpactView> indicators = inPhysicalEquipmentLowImpactViewRepository.findPhysicalEquipmentLowImpactIndicatorsByOrgId(inventoryId);
        indicators.forEach(indicator -> {
                    indicator.setType(TypeUtils.getShortType(subscriber, linkedWorkspace.getName(), indicator.getType()));
                    indicator.setLowImpact(lowImpactService.isLowImpact(indicator.getCountry()));
                }
        );
        return physicalEquipmentIndicatorMapper.inPhysicalEquipmentLowImpacttoDTO(indicators);
    }

    /**
     * Retrieve electric consumption of physical equipments
     *
     * @param taskId the taskId
     * @return electric consumption indicators
     */
    public List<PhysicalEquipmentElecConsumptionBO> getPhysicalEquipmentElecConsumption(final Long taskId, final Long criteriaNumber) {
        final var indicators = inPhysicalEquipmentElecConsumptionViewRepository.findPhysicalEquipmentElecConsumptionIndicators(taskId, criteriaNumber);

        return physicalEquipmentIndicatorMapper.inPhysicalEquipmentElecConsumptionToDto(indicators);
    }
}
