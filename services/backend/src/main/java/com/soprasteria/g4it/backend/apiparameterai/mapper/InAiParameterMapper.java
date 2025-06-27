package com.soprasteria.g4it.backend.apiparameterai.mapper;

import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import com.soprasteria.g4it.backend.server.gen.api.dto.AiParameterRest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface InAiParameterMapper {

    AiParameterRest toBusinessObject(InAiParameter entity);
    InAiParameter toEntity(AiParameterRest aiParameterRest);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AiParameterRest dto, @MappingTarget InAiParameter entity);
}