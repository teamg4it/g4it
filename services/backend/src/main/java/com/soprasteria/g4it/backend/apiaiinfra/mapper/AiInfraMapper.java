package com.soprasteria.g4it.backend.apiaiinfra.mapper;

import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiInfraRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.EquipmentIndicatorRest;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface AiInfraMapper {
    AiInfraBO toBO(AiInfraRest source);
}
