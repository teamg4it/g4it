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
 * Aggregated impact totals for one criterion, filtered by the shared
 * {@link ApplicationCriteriaFilterBO} dimensions. Backing endpoint: §4.1
 * multi-criteria-impacts.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationMultiCriteriaImpactBO {

    private String criteria;

    private String unit;

    private Double impact;

    private Double sip;

    private Long countValue;
}

