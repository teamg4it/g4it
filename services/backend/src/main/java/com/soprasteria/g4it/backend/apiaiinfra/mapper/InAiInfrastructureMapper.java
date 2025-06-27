package com.soprasteria.g4it.backend.apiaiinfra.mapper;

import com.soprasteria.g4it.backend.apiaiinfra.model.InAiInfrastructureBO;
import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import com.soprasteria.g4it.backend.apidigitalservice.model.DeviceTypeBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiInfrastructureRest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface InAiInfrastructureMapper {

    @Mapping(
            target = "infrastructureType",
            expression = "java(source.getInfrastructureType() != null ? source.getInfrastructureType().getCode() : null)")
    InAiInfrastructure toEntity(final InAiInfrastructureRest source);

    InAiInfrastructureBO toBO(InAiInfrastructureRest source);

    /**
     * Map the infrastructureType string to a new DeviceTypeBO infrastructureType object having only the code set.
     * The other parameters of DeviceTypeBO must be retrieved.
     * @param source - The source data that need to be mapped
     * @return The new InAiInfrastructureBO object
     */
    @Mapping(target = "infrastructureType", ignore = true)
    InAiInfrastructureBO entityToBO(InAiInfrastructure source);

    InAiInfrastructureRest toRest(InAiInfrastructureBO source);

    @AfterMapping
    default void entityToBOAfterMapping(InAiInfrastructure source, @MappingTarget InAiInfrastructureBO target) {
        if(source.getInfrastructureType() != null) {
            DeviceTypeBO ref = new DeviceTypeBO();
            ref.setCode(source.getInfrastructureType());
            target.setInfrastructureType(ref);
        }
    }
}
