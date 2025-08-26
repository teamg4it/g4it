/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.mapper;

import com.soprasteria.g4it.backend.apiworkspace.model.OrganizationDetailsBO;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationDetailsRest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganizationDetailsRestMapper {

    OrganizationDetailsRest toDto(final OrganizationDetailsBO organizationDetailsBO);

    List<OrganizationDetailsRest> toDto(final List<OrganizationDetailsBO> lstOrganizationDetailsBO);
}
