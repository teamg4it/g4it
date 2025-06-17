package com.soprasteria.g4it.backend.apiparameterai.mapper;

import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InAiParameterMapper {

    AiParameterRest toBusinessObject(InAiParameter entity);
    InAiParameter toEntity(AiParameterRest aiParameterRest);
}