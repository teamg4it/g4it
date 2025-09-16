/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DateMapper;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = DateMapper.class)
public interface InventoryRestMapper {

    InventoryBO toBusinessObject(final InventoryRest dto);

    InventoryRest toDto(final InventoryBO entity);

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "workspaceStatus", source = "workspace.status")
    List<InventoryRest> toRest(final List<InventoryBO> entities);
}
