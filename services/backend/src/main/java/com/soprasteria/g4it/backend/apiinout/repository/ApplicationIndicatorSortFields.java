/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import java.util.Map;

/**
 * Whitelists the only DTO field names the UI may sort the §4.4 application
 * table ({@code getApplicationIndicators}) by, mapped to their real
 * {@code out_application} column. The ORDER BY clause of the native query
 * must never be built from raw user input directly - SQL injection risk -
 * so any sort field requested outside this map is silently ignored.
 */
public final class ApplicationIndicatorSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "applicationName", "name",
            "criteria", "criterion",
            "sip", "people_eq_impact",
            "impact", "unit_impact"
    );

    private ApplicationIndicatorSortFields() {
    }
}

