package com.soprasteria.g4it.backend.apiaiservice.mapper;

import com.soprasteria.g4it.backend.apiaiservice.model.AIModelConfigBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIModelConfigRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiModelConfigMapper {

    AIModelConfigRest getModelFromEntity(AIModelConfigBO entity);

    List<AIModelConfigRest> toAIModelConfigRest(final List<AIModelConfigBO> source);
}
