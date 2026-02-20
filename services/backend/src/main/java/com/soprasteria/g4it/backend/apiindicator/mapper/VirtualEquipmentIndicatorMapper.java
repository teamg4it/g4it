/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InVirtualEquipmentLowImpactView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VirtualEquipmentIndicatorMapper {

    VirtualEquipmentLowImpactBO toLowImpactBO(InVirtualEquipmentLowImpactView view);

    List<VirtualEquipmentLowImpactBO> toLowImpactBO(
            List<InVirtualEquipmentLowImpactView> views
    );

    @Mapping(source = "elecConsumption", target = "elecConsumption")
    VirtualEquipmentElecConsumptionBO
    inVirtualEquipmentElecConsumptionToDto(
            InVirtualEquipmentElecConsumptionView source
    );

    List<VirtualEquipmentElecConsumptionBO>
    inVirtualEquipmentElecConsumptionToDto(
            List<InVirtualEquipmentElecConsumptionView> source
    );
}


