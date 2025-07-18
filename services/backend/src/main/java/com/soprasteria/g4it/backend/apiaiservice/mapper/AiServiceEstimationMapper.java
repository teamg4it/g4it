package com.soprasteria.g4it.backend.apiaiservice.mapper;

import com.soprasteria.g4it.backend.external.ecomindai.model.AIServiceEstimationBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.AIServiceEstimationRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiServiceEstimationMapper {

    AIServiceEstimationRest getModelFromEntity(AIServiceEstimationBO entity);

    List<AIServiceEstimationRest> toAIServiceEstimationRest(final List<AIServiceEstimationBO> source);

    List<AIServiceEstimationBO> toAIServiceEstimationBO(final List<AIServiceEstimationRest> source);
}
