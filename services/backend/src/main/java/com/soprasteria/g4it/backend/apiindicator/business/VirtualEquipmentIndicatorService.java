/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.VirtualEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentElecConsumptionViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.InVirtualEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VirtualEquipmentIndicatorService {

    @Autowired
    private final InVirtualEquipmentLowImpactViewRepository inVirtualEquipmentLowImpactViewRepository;

    @Autowired
    private final InVirtualEquipmentElecConsumptionViewRepository inVirtualEquipmentElecConsumptionViewRepository;

    @Autowired
    private final VirtualEquipmentIndicatorMapper virtualEquipmentIndicatorMapper;

    /**
     * The Workspace Service
     */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * The LowImpact Service
     */
    @Autowired
    private LowImpactService lowImpactService;


    public List<VirtualEquipmentLowImpactBO> getVirtualEquipmentsLowImpact(
            final String organization,
            final Long workspaceId,
            final Long inventoryId
    ) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);
        final List<InVirtualEquipmentLowImpactView> indicators = inVirtualEquipmentLowImpactViewRepository.findVirtualEquipmentLowImpactIndicatorsByInventoryId(inventoryId);
        indicators.forEach(indicator -> {
                    indicator.setEquipmentType(TypeUtils.getShortType(organization, linkedWorkspace.getName(), indicator.getEquipmentType()));
                    indicator.setLowImpact(lowImpactService.isLowImpact(indicator.getCountry()));
                }
        );
        return virtualEquipmentIndicatorMapper.toLowImpactBO(indicators);
    }

    public List<VirtualEquipmentElecConsumptionBO>
    getVirtualEquipmentElecConsumption(final Long taskId,
                                       final Long criteriaNumber) {

        final var indicators =
                inVirtualEquipmentElecConsumptionViewRepository
                        .findVirtualEquipmentElecConsumptionIndicators(
                                taskId, criteriaNumber);

        return virtualEquipmentIndicatorMapper
                .inVirtualEquipmentElecConsumptionToDto(indicators);
    }


}

