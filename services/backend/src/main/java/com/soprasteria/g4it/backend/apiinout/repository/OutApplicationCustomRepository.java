/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiindicator.model.ApplicationCriteriaFilterBO;
import com.soprasteria.g4it.backend.apiindicator.model.GraphLevel;
import com.soprasteria.g4it.backend.apiindicator.model.RepartitionType;
import com.soprasteria.g4it.backend.apiinout.repository.projection.HierarchyCountsProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaAggregateProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaImpactProjection;

import java.util.List;

/**
 * Custom, 100% database-side aggregation queries backing the application view
 * optimization feature (solution.md §4.1/§4.2/§4.3). All SUM/COUNT/GROUP BY
 * operations are executed by PostgreSQL via dynamically built native SQL; the
 * Java layer never performs in-memory grouping/aggregation for these methods.
 */
public interface OutApplicationCustomRepository {

    /**
     * §4.1 - aggregated impact totals per criterion, filtered by the shared
     * dimensions.
     */
    List<MultiCriteriaImpactProjection> aggregateMultiCriteriaImpacts(Long taskId, ApplicationCriteriaFilterBO filters);

    /**
     * §4.2/§4.3 - dual-axis (graphLevel x repartition) aggregation, one row per
     * (criterion, nodeLabel, repartitionLabel). {@code nodeLabel} is {@code null}
     * for {@link GraphLevel#GLOBAL}.
     */
    List<MultiCriteriaAggregateProjection> aggregateMultiCriteria(Long taskId,
                                                                   List<String> criteria,
                                                                   GraphLevel graphLevel,
                                                                   RepartitionType repartition,
                                                                   ApplicationCriteriaFilterBO filters);

    /**
     * §4.3 - optional standing hierarchy counts (header KPIs) for the whole
     * current filter scope.
     */
    HierarchyCountsProjection countHierarchy(Long taskId, ApplicationCriteriaFilterBO filters);
}

