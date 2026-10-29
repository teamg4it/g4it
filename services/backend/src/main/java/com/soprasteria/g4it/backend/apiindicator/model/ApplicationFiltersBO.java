/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * §4.0 - distinct environment/equipmentType/lifeCycle values, plus the
 * domain -> subDomains hierarchy, for the application view's filter selectors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFiltersBO {

    private List<String> environment;
    private List<String> equipmentType;
    private List<String> lifeCycle;
    private List<ApplicationDomainHierarchyBO> domains;
}

