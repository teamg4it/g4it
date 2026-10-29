/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Breakdown of one graph node by the requested {@link RepartitionType} axis.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationRepartitionImpactBO {

    private String label;

    private Double impact;

    private Double sip;

    private Long countValue;

    private Long consistentCount;

    private Long inconsistentCount;
}

