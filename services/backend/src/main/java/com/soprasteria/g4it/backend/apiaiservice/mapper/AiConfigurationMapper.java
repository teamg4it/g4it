package com.soprasteria.g4it.backend.apiaiservice.mapper;

import com.soprasteria.g4it.backend.apiaiservice.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.apiaiservice.model.AIModelConfigBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIConfigurationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIModelConfigRest;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiConfigurationMapper {

    AIConfigurationRest getModelFromEntity(AIConfigurationBO entity);

    List<AIConfigurationRest> toAIModelConfigRest(final List<AIConfigurationBO> source);

    List<@Valid AIConfigurationBO> toAIConfigurationBO(final List<AIConfigurationRest> source);

}
