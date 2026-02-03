/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IndicatorRestMapper {

    EquipmentIndicatorRest toDto(final EquipmentIndicatorBO source);

    @Mapping(target = "countValue", source = "quantity")
    EquipmentImpactRest toDto(final EquipmentImpactBO source);

    List<EquipmentIndicatorRest> toDto(final List<EquipmentIndicatorBO> source);

    ApplicationIndicatorRest toApplicationIndicatorDto(final ApplicationIndicatorBO<ApplicationImpactBO> source);

    List<ApplicationIndicatorRest> toApplicationIndicatorDto(final List<ApplicationIndicatorBO<ApplicationImpactBO>> source);

    ApplicationVmIndicatorRest toApplicationVmIndicatorDto(final ApplicationIndicatorBO<ApplicationVmImpactBO> source);

    List<ApplicationVmIndicatorRest> toApplicationVmIndicatorDto(final List<ApplicationIndicatorBO<ApplicationVmImpactBO>> source);

    List<PhysicalEquipmentLowImpactRest> toLowImpactDto(final List<PhysicalEquipmentLowImpactBO> source);

    PhysicalEquipmentLowImpactRest toLowImpactDto(final PhysicalEquipmentLowImpactBO source);

    List<PhysicalEquipmentsAvgAgeRest> toAvgAgeDto(final List<PhysicalEquipmentsAvgAgeBO> source);

    PhysicalEquipmentsAvgAgeRest toAvgAgeDto(final PhysicalEquipmentsAvgAgeBO source);

    List<DataCentersInformationRest> toDataCenterDto(final List<DataCentersInformationBO> source);

    DataCentersInformationRest toDataCenterDto(final DataCentersInformationBO source);

    List<PhysicalEquipmentElecConsumptionRest> toElecConsumptionDto(final List<PhysicalEquipmentElecConsumptionBO> source);


    @Mapping(target = "infrastructureType", source = "infrastructureType")
    @Mapping(target = "nomEntite", source = "nomEntite")
    @Mapping(target = "country", source = "country")
    @Mapping(
            target = "quantite",
            expression = "java(bo.getQuantity() == null ? null : bo.getQuantity().intValue())"
    )
    VirtualEquipmentLowImpactRest toVirtualLowImpactDto(VirtualEquipmentLowImpactBO bo);

    List<VirtualEquipmentLowImpactRest> toVirtualLowImpactDto(
            List<VirtualEquipmentLowImpactBO> bos
    );
}
