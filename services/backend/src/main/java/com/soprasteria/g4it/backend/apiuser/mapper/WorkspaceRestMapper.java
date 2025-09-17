/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DateMapper;
import com.soprasteria.g4it.backend.apiuser.model.WorkspaceBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceRest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * UserRest Mapper.
 */
@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface WorkspaceRestMapper {

    /**
     * Map a business object to dto object.
     *
     * @param businessObject the source.
     * @return the WorkspaceRest.
     */
    WorkspaceRest toDto(final WorkspaceBO businessObject);

    /**
     * Map a business object list to dto object list.
     *
     * @param businessObject the source.
     * @return the WorkspaceRest list.
     */
    List<WorkspaceRest> toDto(final List<WorkspaceBO> businessObject);


}
