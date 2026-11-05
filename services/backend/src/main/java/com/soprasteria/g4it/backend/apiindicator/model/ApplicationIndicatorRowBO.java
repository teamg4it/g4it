/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Aggregated row (one per {@code (applicationName, criterion, unit)}) for the
 * §4.4 paginated table view - lifecycle-step/environment/equipmentType/domain/
 * subDomain variations of the same application+criterion are summed into this
 * single row DB-side (see solution.md §4.4.1), replicating the grouping the
 * Angular frontend used to perform client-side before this endpoint existed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApplicationIndicatorRowBO {

    private String applicationName;

    private String criteria;

    private String unit;

    private Double impact;

    private Double sip;
}

