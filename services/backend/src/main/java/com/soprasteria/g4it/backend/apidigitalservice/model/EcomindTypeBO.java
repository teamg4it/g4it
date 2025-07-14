/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Ecomind Type Business Object.
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
public class EcomindTypeBO {

    private String code;

    private String value;

    private Double lifespan;

    private Double defaultCpuCores;

    private Long defaultGpuCount;

    private Long defaultGpuMemory;

    private Double defaultRamSize;

    private Double defaultDatacenterPue;
}