/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository.projection;

/**
 * JPA projection for the §4.2/§4.3 multi-criteria endpoint: one row per
 * (criterion, nodeLabel, repartitionLabel), already aggregated by the database.
 * <p>
 * {@code getNodeLabel()} is {@code null} for graphLevel = GLOBAL (no hierarchy
 * grouping). The hierarchy count / VM attribute fields are {@code null} except at
 * the graphLevel that requires them (see solution.md §4.3 table).
 */
public interface MultiCriteriaAggregateProjection {

    String getCriterion();

    String getUnit();

    String getNodeLabel();

    String getRepartitionLabel();

    Double getImpact();

    Double getSip();

    Long getCountValue();

    Long getConsistentCount();

    Long getInconsistentCount();

    Long getSubDomainCount();

    Long getApplicationCount();

    String getCluster();

    String getEquipmentType();

    String getEnvironment();
}

