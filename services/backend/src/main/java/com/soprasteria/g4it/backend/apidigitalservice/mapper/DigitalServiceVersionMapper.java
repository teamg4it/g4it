/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

/**
 * DigitalServiceVersion Mapper.
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
@Slf4j
public abstract class DigitalServiceVersionMapper {


    public abstract DigitalServiceVersionBO toBusinessObject(final DigitalServiceVersion entity);

    /**
     * Map DigitalServiceVersion and DigitalService to DigitalServiceVersionBO
     */
    public DigitalServiceVersionBO toBusinessObject(final DigitalServiceVersion version,
                                                    final DigitalService digitalService) {
        DigitalServiceVersionBO bo = toBusinessObject(version);

        // Add DigitalService fields
        bo.setDsvUid(version.getUid());
        bo.setName(digitalService.getName());
        bo.setIsAi(digitalService.isAi());
        bo.setEnableDataInconsistency(digitalService.isEnableDataInconsistency());
        bo.setIsShared(false); // Default value, can be set later
        bo.setVersionName(version.getVersionName());


        return bo;
    }
}
