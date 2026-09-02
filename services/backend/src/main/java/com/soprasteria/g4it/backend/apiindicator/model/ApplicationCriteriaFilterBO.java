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
 * Shared filter dimensions for the application view aggregation endpoints
 * (multi-criteria-impacts, multi-criteria, hierarchy counts). All fields are
 * optional; several values on the same dimension are combined with OR, different
 * dimensions are combined with AND.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationCriteriaFilterBO {

    private List<String> environment;

    private List<String> equipmentType;

    private List<String> lifeCycle;

    private List<String> domain;

    private List<String> subDomain;

    private List<String> applicationName;
}

