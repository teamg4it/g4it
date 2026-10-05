/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.DataCentersInformationBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InDatacenterIndicatorView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DataCenterIndicatorMapper {


    List<DataCentersInformationBO> toInDataCentersDto(final List<InDatacenterIndicatorView> source);

    @Mapping(target = "dataCenterName", source = "name")
    @Mapping(target = "country", source = "location")
    @Mapping(target = "entity", source = "entity")
    @Mapping(target = "equipment", source = "equipment")
    @Mapping(target = "status", source = "status")
    DataCentersInformationBO toInDataCentersDto(final InDatacenterIndicatorView source);

}
