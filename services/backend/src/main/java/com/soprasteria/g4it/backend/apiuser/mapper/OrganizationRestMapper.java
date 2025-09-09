/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.mapper;

import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.List;

/**
 * UserRest Mapper.
 */
@Mapper(componentModel = "spring", uses = WorkspaceRestMapper.class)
public interface OrganizationRestMapper {

    /**
     * Map a business object to dto object.
     *
     * @param businessObject the source.
     * @return the OrganizationRest.
     */
    // @Mapping(source = "workspaces", target = "workspaces")
    OrganizationRest toDto(final OrganizationBO businessObject);

    /**
     * Map a comma-separated string of authorized domains to a list of strings.
     *
     * @param authorizedDomains the comma-separated string of authorized domains.
     * @return a list of authorized domains, or null if the input is null.
     */
    default List<String> mapAuthorizedDomains(String authorizedDomains) {
        if (authorizedDomains == null) return List.of();
        return List.of(authorizedDomains.split(","));
    }

    /**
     * Map a business object list to dto object list.
     *
     * @param businessObject the source.
     * @return the OrganizationRest list.
     */
    List<OrganizationRest> toDto(final List<OrganizationBO> businessObject);

    /**
     * Map an entity to business object.
     *
     * @param organization the OrganizationBO
     * @return the OrganizationBO.
     */
    OrganizationBO toBusinessObject(final Organization organization);

}
