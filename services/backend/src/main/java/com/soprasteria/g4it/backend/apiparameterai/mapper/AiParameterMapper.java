package com.soprasteria.g4it.backend.apiparameterai.mapper;

import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiParameterMapper {

    AiParameterRest toBusinessObject(AiParameter entity);
    AiParameter toEntity(AiParameterRest aiParameterRest);
    AiParameterRest toDto(AiParameterRest aiParameterRest);
}