/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.EquipmentImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipmentIndicatorMapper {

    List<EquipmentImpactBO> toOldImpact(final List<OutPhysicalEquipment> source);

    @Mapping(target = "acvStep", source = "lifecycleStep")
    @Mapping(target = "country", source = "location")
    @Mapping(target = "equipment", source = "equipmentType")
    @Mapping(target = "entity", expression = "java(source.getCommonFilters().get(0))")
    @Mapping(target = "status", expression = "java(source.getFilters().get(0))")
    @Mapping(target = "impact", source = "unitImpact")
    @Mapping(target = "sip", source = "peopleEqImpact")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "statusIndicator", source = "statusIndicator")
    @Mapping(target = "countValue", source = "countValue")
    EquipmentImpactBO toImpact(final OutPhysicalEquipment source);

    @Mapping(target = "impacts", source = "source")
    default EquipmentIndicatorBO outToDto(final List<OutPhysicalEquipment> source) {
        return EquipmentIndicatorBO.builder()
                .unit(source.get(0).getUnit())
                .label(source.get(0).getCriterion())
                .impacts(toOldImpact(source))
                .build();
    }

}
