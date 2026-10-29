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
 * Standing hierarchy counts (header KPIs) for the whole current filter scope,
 * independent of any single drill node. Backing endpoint: §4.3 optional
 * multi-criteria/counts.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationHierarchyCountsBO {

    private Long domainCount;

    private Long subDomainCount;

    private Long applicationCount;

    private Long virtualEquipmentCount;
}

