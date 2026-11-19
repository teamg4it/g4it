/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository.projection;

/**
 * One already-aggregated row (one per {@code (applicationName, criterion, unit)})
 * for the §4.4 paginated table view. Backs the fix described in
 * spec/applicationViewOptimizationFeature/solution.md §4.4.1: the database
 * groups every lifecycle-step/environment/equipmentType/domain/subDomain
 * variation of a given application+criterion into a single summed row
 * (SUM(unit_impact), SUM(people_eq_impact)) - replicating, 100% DB-side,
 * the grouping the Angular frontend used to perform client-side before the
 * migration to this paginated endpoint. No in-memory grouping/summing.
 */
public interface ApplicationIndicatorRowProjection {

    String getApplicationName();

    String getCriterion();

    String getUnit();

    Double getImpact();

    Double getSip();
}

