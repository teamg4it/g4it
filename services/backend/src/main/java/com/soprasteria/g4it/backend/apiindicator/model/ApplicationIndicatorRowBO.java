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
 * Flattened row (one per application/criterion) for the §4.4 paginated table
 * view - avoids the "grouped by criteria" shape needed for the graph endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApplicationIndicatorRowBO {

    private String criteria;

    private String unit;

    private String lifeCycle;

    private String domain;

    private String subDomain;

    private String environment;

    private String equipmentType;

    private String applicationName;

    private String virtualEquipmentName;

    private String cluster;

    private Double impact;

    private Double sip;

    private String statusIndicator;

    private String provider;

    private String location;
}

