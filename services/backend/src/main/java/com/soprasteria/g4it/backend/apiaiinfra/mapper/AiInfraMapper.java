package com.soprasteria.g4it.backend.apiaiinfra.mapper;

import com.soprasteria.g4it.backend.apiaiinfra.model.AiInfraBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface AiInfraMapper {
    AiInfraBO toBO(InAiInfrastructureRest source);

    InAiInfrastructure toEntity(InAiInfrastructureRest source);
}
