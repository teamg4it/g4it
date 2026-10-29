/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.OutNonItActivity;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutNonItActivityRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OutNonItActivityMapper {

    List<OutNonItActivityRest> toRest(final List<OutNonItActivity> source);
}
