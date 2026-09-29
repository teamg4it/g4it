/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.server.gen.api.dto.InAiServiceRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * in AI service mapper.
 */
@Mapper(componentModel = "spring")
public interface InAiServiceMapper {

    List<InAiServiceRest> toRest(final List<InAiService> source);

    InAiServiceRest toRest(final InAiService source);

    InAiService toEntity(final InAiServiceRest source);
}
