package com.soprasteria.g4it.backend.apiaiinfra.mapper;

import com.soprasteria.g4it.backend.apiaiinfra.model.InAiInfrastructureBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface InAiInfrastructureMapper {

    InAiInfrastructure toEntity(final InAiInfrastructureRest source);

    InAiInfrastructureBO toBO(InAiInfrastructureRest source);

    InAiInfrastructureBO entityToBO(InAiInfrastructure source);

    InAiInfrastructureRest toRest(InAiInfrastructureBO source);
}
