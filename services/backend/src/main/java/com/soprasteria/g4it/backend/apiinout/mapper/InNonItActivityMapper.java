/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InNonItActivity;
import com.soprasteria.g4it.backend.server.gen.api.dto.InNonItActivityRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * in Non-IT Activity mapper.
 */
@Mapper(componentModel = "spring")
public interface InNonItActivityMapper {

    List<InNonItActivityRest> toRest(final List<InNonItActivity> source);

    InNonItActivityRest toRest(final InNonItActivity source);

    InNonItActivity toEntity(final InNonItActivityRest source);
}
