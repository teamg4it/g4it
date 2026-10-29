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
 * §4.0 - one domain and the distinct subDomains linked to it, used to populate
 * the application view's domain/subDomain filter tree.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDomainHierarchyBO {

    private String domain;
    private List<String> subDomains;
}

