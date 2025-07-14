package com.soprasteria.g4it.backend.apiaiservice.mapper;

import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.LLMModelConfig;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIModelConfigBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIModelConfigRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiModelConfigMapper {

    AIModelConfigRest getModelFromEntity(AIModelConfigBO entity);

    @Mapping(source = "nbParameters", target = "parameters")
    AIModelConfigRest toAIModelConfigRest(final LLMModelConfig source);

    List<AIModelConfigRest> toAIModelConfigRest(final List<LLMModelConfig> source);
}
