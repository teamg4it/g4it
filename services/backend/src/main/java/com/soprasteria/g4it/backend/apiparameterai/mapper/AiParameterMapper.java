package com.soprasteria.g4it.backend.apiparameterai.mapper;

import com.soprasteria.g4it.backend.apiparameterai.model.AiParameterBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiParameterMapper {

    AiParameterBO toBusinessObject(AiParameter entity);
    AiParameter toEntity(AiParameterBO aiParameterBO);

    AiParameterRest toDto(AiParameterBO aiParameterBO);
    AiParameterBO toBusinessObject(AiParameterRest aiParameterRest);
}