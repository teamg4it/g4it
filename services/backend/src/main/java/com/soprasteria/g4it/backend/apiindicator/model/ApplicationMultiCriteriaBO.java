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

import java.util.List;

/**
 * Root response element of the multi-criteria endpoint: one entry per requested
 * criterion, containing the graph nodes for the currently requested
 * {@link GraphLevel} (a single node at GLOBAL, one node per hierarchy value from
 * DOMAIN onward), each broken down internally by the requested
 * {@link RepartitionType}.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationMultiCriteriaBO {

    private String criteria;

    private String unit;

    private List<ApplicationNodeBO> nodes;
}

