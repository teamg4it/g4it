/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository.projection;

/**
 * JPA projection for the §4.1 multi-criteria-impacts endpoint: one row per
 * criterion, already aggregated by the database (SUM/COUNT/GROUP BY).
 */
public interface MultiCriteriaImpactProjection {

    String getCriterion();

    String getUnit();

    Double getImpact();

    Double getSip();

    Long getCountValue();
}

