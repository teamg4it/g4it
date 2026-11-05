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
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationFiltersProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaAggregateProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaImpactProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentElecConsumptionSumProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentLowImpactCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * §4.0 - distinct environment/equipmentType/lifeCycle/domain/subDomain values
     * for the given task, used to populate the application view's filter selectors.
     */
    ApplicationFiltersProjection getDistinctFilters(Long taskId);

    /**
     * §4.4 - DB-side filtered + paginated (WHERE + LIMIT/OFFSET) listing of
     * application rows for the table view, applying the same shared filter
     * dimensions as §4.1-4.3.
     */
    Page<OutApplication> findByTaskId(Long taskId, ApplicationCriteriaFilterBO filters, Pageable pageable);

    /**
     * Aggregated total and low-impact virtual equipment counts, filtered by the
     * shared dimensions. Joins out_application with in_virtual_equipment/in_datacenter
     * to resolve location, and with ref_item_impact (electricity-mix) to compute the
     * low-impact flag per country entirely in SQL (quartile of summed per-criterion
     * quartiles). isoCodes/countryNames bind the external (Boavizta) ISO -> country
     * name mapping, since it is not itself stored in the database.
     */
    VirtualEquipmentLowImpactCountProjection countVirtualEquipmentLowImpact(Long taskId,
                                                                             Long inventoryId,
                                                                             Long workspaceId,
                                                                             String[] criteria,
                                                                             String[] isoCodes,
                                                                             String[] countryNames,
                                                                             ApplicationCriteriaFilterBO filters);

    /**
     * Aggregated electricity consumption sum and virtual equipment quantity sum,
     * filtered by the shared dimensions. Replicates the exact two-level GROUP BY
     * used by the legacy {@code InVirtualEquipmentElecConsumptionView} native
     * query (dedup by virtual_equipment_name/infrastructure_type/quantity, then
     * re-aggregated per application collapsing CLOUD_SERVICES vs non-cloud
     * infrastructure types) before summing, to stay consistent with the legacy
     * per-row endpoint.
     */
    VirtualEquipmentElecConsumptionSumProjection sumVirtualEquipmentElecConsumption(Long taskId,
                                                                                     ApplicationCriteriaFilterBO filters);
}
