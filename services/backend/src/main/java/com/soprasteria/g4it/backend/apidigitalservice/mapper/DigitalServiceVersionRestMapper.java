/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * DigitalServiceRest Mapper.
 */
@Mapper(componentModel = "spring",
        uses = {DateMapper.class})
public interface DigitalServiceVersionRestMapper {

    /**
     * Map to Data Transfer Object.
     *
     * @param businessObject the source.
     * @return the DigitalServiceVersionRest.
     */
    DigitalServiceVersionRest toDto(final DigitalServiceVersionBO businessObject);

    List<DigitalServiceVersionRest> toDto(final List<DigitalServiceVersionBO> businessObjects);


}
