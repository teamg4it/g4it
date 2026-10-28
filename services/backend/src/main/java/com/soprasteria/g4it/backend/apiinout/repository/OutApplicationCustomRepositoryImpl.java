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
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationDomainHierarchyProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationFiltersProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.HierarchyCountsProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaAggregateProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaImpactProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentElecConsumptionSumProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentLowImpactCountProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link OutApplicationCustomRepository}. Every method builds a
 * single native SQL statement, executed once against PostgreSQL, doing all
 * filtering (WHERE) and aggregation (GROUP BY / SUM / COUNT / COUNT(*) FILTER)
 * in the database. Java code only maps the small, already-aggregated result set
 * into projection POJOs - no in-memory grouping/summing (solution.md §6).
 * <p>
 * Column selection for {@code graphLevel}/{@code repartition} is resolved from a
 * fixed, internal whitelist (never from raw user input), so there is no SQL
 * injection risk despite the query being built dynamically.
 */
@Repository
@Slf4j
public class OutApplicationCustomRepositoryImpl implements OutApplicationCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Maps a repartition axis to its real SQL column.
     */
    private static String repartitionColumn(final RepartitionType repartition) {
        return switch (repartition) {
            case LIFE_CYCLE -> "lifecycle_step";
            case ENVIRONMENT -> "environment";
            case EQUIPMENT_TYPE -> "equipment_type";
        };
    }

    /**
     * Maps a graph level to its SQL node-label expression. {@code null} for
     * {@link GraphLevel#GLOBAL} (no hierarchy grouping).
     */
    private static String nodeLabelExpression(final GraphLevel graphLevel) {
        return switch (graphLevel) {
            case GLOBAL -> null;
            case DOMAIN -> "filters[1]";
            case SUB_DOMAIN -> "filters[2]";
            case APPLICATION -> "name";
            case VIRTUAL_EQUIPMENT -> "virtual_equipment_name";
        };
    }

    @Override
    public List<MultiCriteriaImpactProjection> aggregateMultiCriteriaImpacts(final Long taskId,
                                                                              final ApplicationCriteriaFilterBO filters) {
        final StringBuilder sql = new StringBuilder("""
                SELECT criterion, unit,
                       SUM(unit_impact)      AS impact,
                       SUM(people_eq_impact) AS sip,
                       COUNT(*)              AS count_value
                FROM out_application
                WHERE task_id = :taskId
                """);
        final List<Object[]> bindings = new ArrayList<>();
        appendFilterPredicates(sql, bindings, filters);
        sql.append(" GROUP BY criterion, unit");

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        bindParameters(query, bindings);

        final List<Object[]> rows = query.getResultList();
        final List<MultiCriteriaImpactProjection> result = new ArrayList<>(rows.size());
        for (final Object[] row : rows) {
            result.add(new MultiCriteriaImpactProjectionImpl(
                    (String) row[0],
                    (String) row[1],
                    toDouble(row[2]),
                    toDouble(row[3]),
                    toLong(row[4])
            ));
        }
        return result;
    }

    @Override
    public List<MultiCriteriaAggregateProjection> aggregateMultiCriteria(final Long taskId,
                                                                          final List<String> criteria,
                                                                          final GraphLevel graphLevel,
                                                                          final RepartitionType repartition,
                                                                          final ApplicationCriteriaFilterBO filters) {
        final String nodeLabel = nodeLabelExpression(graphLevel);
        final String repartitionLabel = repartitionColumn(repartition);
        final boolean hasNode = nodeLabel != null;

        final StringBuilder select = new StringBuilder("SELECT criterion, unit, ");
        select.append(hasNode ? nodeLabel + " AS node_label, " : "NULL AS node_label, ");
        select.append(repartitionLabel).append(" AS repartition_label, ")
                .append("SUM(unit_impact) AS impact, ")
                .append("SUM(people_eq_impact) AS sip, ")
                .append("COUNT(*) AS count_value, ")
                .append("COUNT(*) FILTER (WHERE status_indicator = 'OK') AS consistent_count, ")
                .append("COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count");

        // level-specific extra aggregates - only what that level needs (solution.md §4.3 table)
        final boolean needsSubDomainCount = graphLevel == GraphLevel.DOMAIN;
        final boolean needsApplicationCount = graphLevel == GraphLevel.DOMAIN || graphLevel == GraphLevel.SUB_DOMAIN;
        final boolean needsVmAttributes = graphLevel == GraphLevel.VIRTUAL_EQUIPMENT;

        if (needsSubDomainCount) {
            select.append(", COUNT(DISTINCT filters[2]) AS sub_domain_count");
        }
        if (needsApplicationCount) {
            select.append(", COUNT(DISTINCT name) AS application_count");
        }
        if (needsVmAttributes) {
            select.append(", MIN(filters_virtual_equipment[1]) AS cluster")
                    .append(", MIN(equipment_type) AS equipment_type_attr")
                    .append(", MIN(environment) AS environment_attr");
        }

        final StringBuilder sql = new StringBuilder(select)
                .append(" FROM out_application WHERE task_id = :taskId");

        final List<Object[]> bindings = new ArrayList<>();

        // criteria is optional only at graphLevel = GLOBAL (null/empty = all criteria
        // present for the task); the controller enforces non-empty criteria at every
        // other graphLevel - see solution.md §4.2/§6. Reuses the same null/empty-skip
        // convention as every other optional filter dimension.
        appendInPredicate(sql, bindings, "criterion", criteria);

        // click-path / hierarchy scoping filters, applied on top of the shared filter dimensions
        if (graphLevel == GraphLevel.SUB_DOMAIN || graphLevel == GraphLevel.APPLICATION
                || graphLevel == GraphLevel.VIRTUAL_EQUIPMENT) {
            appendInPredicate(sql, bindings, "filters[1]", filters == null ? null : filters.getDomain());
        }
        if (graphLevel == GraphLevel.APPLICATION || graphLevel == GraphLevel.VIRTUAL_EQUIPMENT) {
            appendInPredicate(sql, bindings, "filters[2]", filters == null ? null : filters.getSubDomain());
        }
        if (graphLevel == GraphLevel.VIRTUAL_EQUIPMENT) {
            appendInPredicate(sql, bindings, "name", filters == null ? null : filters.getApplicationName());
        }
        appendFilterPredicates(sql, bindings, filters);

        sql.append(" GROUP BY criterion, unit, ");
        if (hasNode) {
            sql.append(nodeLabel).append(", ");
        }
        sql.append(repartitionLabel);

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        bindParameters(query, bindings);

        final List<Object[]> rows = query.getResultList();
        final List<MultiCriteriaAggregateProjection> result = new ArrayList<>(rows.size());
        for (final Object[] row : rows) {
            int i = 0;
            final String rCriterion = (String) row[i++];
            final String rUnit = (String) row[i++];
            final String rNodeLabel = (String) row[i++];
            final String rRepartitionLabel = (String) row[i++];
            final Double rImpact = toDouble(row[i++]);
            final Double rSip = toDouble(row[i++]);
            final Long rCountValue = toLong(row[i++]);
            final Long rConsistent = toLong(row[i++]);
            final Long rInconsistent = toLong(row[i++]);
            final Long rSubDomainCount = needsSubDomainCount ? toLong(row[i++]) : null;
            final Long rApplicationCount = needsApplicationCount ? toLong(row[i++]) : null;
            final String rCluster = needsVmAttributes ? (String) row[i++] : null;
            final String rEquipmentType = needsVmAttributes ? (String) row[i++] : null;
            final String rEnvironment = needsVmAttributes ? (String) row[i++] : null;

            result.add(new MultiCriteriaAggregateProjectionImpl(
                    rCriterion, rUnit, rNodeLabel, rRepartitionLabel, rImpact, rSip, rCountValue,
                    rConsistent, rInconsistent, rSubDomainCount, rApplicationCount,
                    rCluster, rEquipmentType, rEnvironment
            ));
        }
        return result;
    }

    @Override
    public HierarchyCountsProjection countHierarchy(final Long taskId, final ApplicationCriteriaFilterBO filters) {
        final StringBuilder sql = new StringBuilder("""
                SELECT COUNT(DISTINCT filters[1])         AS domain_count,
                       COUNT(DISTINCT filters[2])         AS sub_domain_count,
                       COUNT(DISTINCT name)                AS application_count,
                       COUNT(DISTINCT virtual_equipment_name) AS virtual_equipment_count
                FROM out_application
                WHERE task_id = :taskId
                """);
        final List<Object[]> bindings = new ArrayList<>();
        appendFilterPredicates(sql, bindings, filters);

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        bindParameters(query, bindings);

        final Object[] row = (Object[]) query.getSingleResult();
        return new HierarchyCountsProjectionImpl(toLong(row[0]), toLong(row[1]), toLong(row[2]), toLong(row[3]));
    }

    /**
     * Aggregated total and low-impact virtual equipment counts, filtered by the
     * shared dimensions. Entirely computed in SQL:
     * <ul>
     *     <li>{@code iso_map} binds the external (Boavizta) ISO -&gt; country name
     *     mapping, since that mapping is not itself stored in the database;</li>
     *     <li>{@code criterion_percentiles}/{@code criterion_quartile}/{@code country_score}/
     *     {@code threshold}/{@code country_low_impact} replicate {@code LowImpactService.isLowImpact}
     *     (per-criterion electricity-mix quartile, summed per country, compared to
     *     the first quartile of all countries). Note: PostgreSQL does not support
     *     {@code PERCENTILE_CONT(...) OVER (PARTITION BY ...)} (ordered-set aggregates
     *     cannot be used as window functions - SQLState 0A000), so percentiles per
     *     criterion are pre-computed with a plain {@code GROUP BY} in
     *     {@code criterion_percentiles} and then joined back onto each row;</li>
     *     <li>{@code distinct_applications} replicates the exact collapsing GROUP BY
     *     used by the legacy {@code InVirtualEquipmentLowImpactView} native query
     *     (virtual_equipment_name, name, location, lifecycle_step, domain, subDomain,
     *     environment, equipment_type) - {@code out_application} has one row per
     *     evaluated criterion for the same application, so this step is required to
     *     avoid counting the same application multiple times, and keeps this endpoint
     *     consistent with the legacy per-row endpoint's quantity/lowImpact semantics
     *     (quantity is always 1 per group there, since {@code name} is itself part
     *     of that GROUP BY);</li>
     *     <li>the final SELECT counts that deduplicated set, total vs. low-impact.</li>
     * </ul>
     */
    @Override
    public VirtualEquipmentLowImpactCountProjection countVirtualEquipmentLowImpact(final Long taskId,
                                                                                    final Long inventoryId,
                                                                                    final Long workspaceId,
                                                                                    final String[] criteria,
                                                                                    final String[] isoCodes,
                                                                                    final String[] countryNames,
                                                                                    final ApplicationCriteriaFilterBO filters) {
        final StringBuilder sql = new StringBuilder("""
                WITH iso_map AS (
                    SELECT iso, country FROM unnest(:isoCodes, :countryNames) AS t(iso, country)
                ),
                criterion_percentiles AS (
                    SELECT
                        criterion,
                        PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY value) AS p25,
                        PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY value) AS p50,
                        PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY value) AS p75
                    FROM ref_item_impact
                    WHERE category = 'electricity-mix'
                      AND criterion = ANY(:criteria)
                      AND (workspace_id = :workspaceId OR workspace_id IS NULL)
                    GROUP BY criterion
                ),
                criterion_quartile AS (
                    SELECT
                        ri.location AS country,
                        CASE
                            WHEN ri.value <= cp.p25 THEN 1
                            WHEN ri.value <= cp.p50 THEN 2
                            WHEN ri.value <= cp.p75 THEN 3
                            ELSE 4
                        END AS quartile
                    FROM ref_item_impact ri
                    JOIN criterion_percentiles cp ON cp.criterion = ri.criterion
                    WHERE ri.category = 'electricity-mix'
                      AND ri.criterion = ANY(:criteria)
                      AND (ri.workspace_id = :workspaceId OR ri.workspace_id IS NULL)
                ),
                country_score AS (
                    SELECT country, SUM(quartile) AS total_score FROM criterion_quartile GROUP BY country
                ),
                threshold AS (
                    SELECT PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY total_score) AS q1 FROM country_score
                ),
                country_low_impact AS (
                    SELECT cs.country, (cs.total_score < t.q1) AS low_impact
                    FROM country_score cs CROSS JOIN threshold t
                ),
                distinct_applications AS (
                    SELECT
                        oa.name AS application_name,
                        COALESCE(dc.location, ive.location, 'Unknown') AS location
                    FROM out_application oa
                    JOIN in_virtual_equipment ive
                          ON ive.name = oa.virtual_equipment_name AND ive.inventory_id = :inventoryId
                    LEFT JOIN in_datacenter dc
                          ON dc.inventory_id = ive.inventory_id AND dc.name = ive.datacenter_name
                    WHERE oa.task_id = :taskId AND oa.status_indicator = 'OK'
                """);

        final List<Object[]> bindings = new ArrayList<>();
        if (filters != null) {
            appendInPredicate(sql, bindings, "oa.environment", filters.getEnvironment());
            appendInPredicate(sql, bindings, "oa.equipment_type", filters.getEquipmentType());
            appendInPredicate(sql, bindings, "oa.lifecycle_step", filters.getLifeCycle());
            appendInPredicate(sql, bindings, "oa.filters[1]", filters.getDomain());
            appendInPredicate(sql, bindings, "oa.filters[2]", filters.getSubDomain());
            appendInPredicate(sql, bindings, "oa.name", filters.getApplicationName());
        }

        sql.append("""
                    GROUP BY
                        oa.virtual_equipment_name,
                        oa.name,
                        COALESCE(dc.location, ive.location, 'Unknown'),
                        oa.lifecycle_step,
                        oa.filters[1],
                        oa.filters[2],
                        oa.environment,
                        oa.equipment_type
                )
                SELECT
                    COUNT(*)                                                AS total_count,
                    COUNT(*) FILTER (WHERE COALESCE(cli.low_impact, false)) AS low_impact_count
                FROM distinct_applications da
                LEFT JOIN iso_map im ON im.iso = da.location
                LEFT JOIN country_low_impact cli ON cli.country = im.country
                """);

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        query.setParameter("inventoryId", inventoryId);
        query.setParameter("workspaceId", workspaceId);
        query.setParameter("criteria", criteria);
        query.setParameter("isoCodes", isoCodes);
        query.setParameter("countryNames", countryNames);
        bindParameters(query, bindings);
        final Object[] row = (Object[]) query.getSingleResult();
        return new VirtualEquipmentLowImpactCountProjectionImpl(toLong(row[0]), toLong(row[1]));
    }

    /**
     * Aggregated electricity consumption sum and virtual equipment quantity sum,
     * filtered by the shared dimensions. Entirely computed in SQL, replicating the
     * exact two-level GROUP BY used by the legacy {@code InVirtualEquipmentElecConsumptionView}
     * native query:
     * <ul>
     *     <li>{@code distinct_applications} replicates the inner "sub" subquery:
     *     one row per (virtual_equipment_name, location, domain, subDomain,
     *     environment, equipmentType, application name, infrastructure_type,
     *     quantity), restricted to the USING lifecycle step (the only step for
     *     which electricity consumption is computed);</li>
     *     <li>{@code application_elec_consumption} replicates the outer
     *     aggregation: per application, sums the scaled electricity consumption
     *     of all non-CLOUD_SERVICES virtual equipment plus (at most once) the
     *     CLOUD_SERVICES one, and takes the virtual equipment quantity - this is
     *     the same row-per-application granularity as the legacy
     *     {@code InVirtualEquipmentElecConsumptionView} rows;</li>
     *     <li>the final SELECT sums elecConsumption/quantity over that
     *     deduplicated, filtered set.</li>
     * </ul>
     */
    @Override
    public VirtualEquipmentElecConsumptionSumProjection sumVirtualEquipmentElecConsumption(
            final Long taskId, final ApplicationCriteriaFilterBO filters) {
        final StringBuilder sql = new StringBuilder("""
                WITH distinct_applications AS (
                    SELECT
                        oa.virtual_equipment_name,
                        COALESCE(dc.location, ive.location, 'Unknown') AS location,
                        oa.filters[1] AS domain,
                        oa.filters[2] AS sub_domain,
                        oa.environment,
                        oa.equipment_type,
                        oa.name AS application_name,
                        ive.infrastructure_type,
                        ive.quantity AS virtual_equipment_quantity,
                        MAX(oa.electricity_consumption) * ive.quantity AS vm_elec_scaled
                    FROM out_application oa
                    JOIN task t ON t.id = oa.task_id AND t.status = 'COMPLETED' AND t.id = :taskId
                    JOIN in_virtual_equipment ive
                          ON ive.name = oa.virtual_equipment_name AND ive.inventory_id = t.inventory_id
                    LEFT JOIN in_datacenter dc
                          ON dc.name = ive.datacenter_name AND dc.inventory_id = ive.inventory_id
                    WHERE oa.lifecycle_step = 'USING'
                    GROUP BY
                        oa.virtual_equipment_name,
                        COALESCE(dc.location, ive.location, 'Unknown'),
                        oa.filters[1],
                        oa.filters[2],
                        oa.environment,
                        oa.equipment_type,
                        oa.name,
                        ive.infrastructure_type,
                        ive.quantity
                ),
                application_elec_consumption AS (
                    SELECT
                        da.virtual_equipment_name,
                        da.location,
                        da.domain,
                        da.sub_domain,
                        da.environment,
                        da.equipment_type,
                        da.application_name,
                        SUM(CASE WHEN da.infrastructure_type <> 'CLOUD_SERVICES' THEN da.vm_elec_scaled ELSE 0 END)
                            + COALESCE(MAX(CASE WHEN da.infrastructure_type = 'CLOUD_SERVICES' THEN da.vm_elec_scaled END), 0) AS elec_consumption,
                        MAX(da.virtual_equipment_quantity) AS quantity
                    FROM distinct_applications da
                    GROUP BY
                        da.virtual_equipment_name,
                        da.location,
                        da.domain,
                        da.sub_domain,
                        da.environment,
                        da.equipment_type,
                        da.application_name
                )
                SELECT
                    COALESCE(SUM(elec_consumption), 0) AS elec_consumption_sum,
                    COALESCE(SUM(quantity), 0)          AS quantity_sum,
                    COUNT(DISTINCT application_name)    AS application_count
                FROM application_elec_consumption
                WHERE 1 = 1
                """);

        final List<Object[]> bindings = new ArrayList<>();
        if (filters != null) {
            appendInPredicate(sql, bindings, "environment", filters.getEnvironment());
            appendInPredicate(sql, bindings, "equipment_type", filters.getEquipmentType());
            appendInPredicate(sql, bindings, "domain", filters.getDomain());
            appendInPredicate(sql, bindings, "sub_domain", filters.getSubDomain());
            appendInPredicate(sql, bindings, "application_name", filters.getApplicationName());
        }

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        bindParameters(query, bindings);
        final Object[] row = (Object[]) query.getSingleResult();
        return new VirtualEquipmentElecConsumptionSumProjectionImpl(toDouble(row[0]), toLong(row[1]), toLong(row[2]));
    }

    @Override
    public ApplicationFiltersProjection getDistinctFilters(final Long taskId) {
        final Query scalarsQuery = entityManager.createNativeQuery("""
                SELECT
                    array_agg(DISTINCT environment)    FILTER (WHERE environment    IS NOT NULL) AS environments,
                    array_agg(DISTINCT equipment_type) FILTER (WHERE equipment_type IS NOT NULL) AS equipment_types,
                    array_agg(DISTINCT lifecycle_step) FILTER (WHERE lifecycle_step IS NOT NULL) AS lifecycle_steps
                FROM out_application
                WHERE task_id = :taskId
                """);
        scalarsQuery.setParameter("taskId", taskId);
        final Object[] scalarRow = (Object[]) scalarsQuery.getSingleResult();

        // domain -> subDomains tree, built entirely DB-side via GROUP BY + array_agg
        // so subDomains stay correctly linked to their parent domain.
        final Query domainsQuery = entityManager.createNativeQuery("""
                SELECT filters[1] AS domain,
                       array_agg(DISTINCT filters[2]) FILTER (WHERE filters[2] IS NOT NULL) AS sub_domains
                FROM out_application
                WHERE task_id = :taskId AND filters[1] IS NOT NULL
                GROUP BY filters[1]
                ORDER BY filters[1]
                """);
        domainsQuery.setParameter("taskId", taskId);

        final List<Object[]> domainRows = domainsQuery.getResultList();
        final List<ApplicationDomainHierarchyProjection> domains = new ArrayList<>(domainRows.size());
        for (final Object[] domainRow : domainRows) {
            domains.add(new ApplicationDomainHierarchyProjectionImpl((String) domainRow[0], toStringList(domainRow[1])));
        }

        return new ApplicationFiltersProjectionImpl(
                toStringList(scalarRow[0]),
                toStringList(scalarRow[1]),
                toStringList(scalarRow[2]),
                domains
        );
    }

    @Override
    public Page<OutApplication> findByTaskId(final Long taskId, final ApplicationCriteriaFilterBO filters,
                                              final Pageable pageable) {
        final StringBuilder dataSql = new StringBuilder("SELECT * FROM out_application WHERE task_id = :taskId");
        final List<Object[]> dataBindings = new ArrayList<>();
        appendFilterPredicates(dataSql, dataBindings, filters);
        appendInPredicate(dataSql, dataBindings, "criterion", filters == null ? null : filters.getCriterion());
        dataSql.append(" ORDER BY id");

        final StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM out_application WHERE task_id = :taskId");
        final List<Object[]> countBindings = new ArrayList<>();
        appendFilterPredicates(countSql, countBindings, filters);
        appendInPredicate(countSql, countBindings, "criterion", filters == null ? null : filters.getCriterion());

        final Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), OutApplication.class);
        dataQuery.setParameter("taskId", taskId);
        bindParameters(dataQuery, dataBindings);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        final Query countQuery = entityManager.createNativeQuery(countSql.toString());
        countQuery.setParameter("taskId", taskId);
        bindParameters(countQuery, countBindings);

        @SuppressWarnings("unchecked")
        final List<OutApplication> content = dataQuery.getResultList();
        final long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Appends the shared filter dimensions (environment / equipmentType / lifeCycle
     * / domain / subDomain) as {@code AND (col = ANY(:param))} predicates, skipping
     * any dimension that is null/empty.
     */
    private static void appendFilterPredicates(final StringBuilder sql,
                                                final List<Object[]> bindings,
                                                final ApplicationCriteriaFilterBO filters) {        if (filters == null) {
            return;
        }
        appendInPredicate(sql, bindings, "environment", filters.getEnvironment());
        appendInPredicate(sql, bindings, "equipment_type", filters.getEquipmentType());
        appendInPredicate(sql, bindings, "lifecycle_step", filters.getLifeCycle());
        appendInPredicate(sql, bindings, "filters[1]", filters.getDomain());
        appendInPredicate(sql, bindings, "filters[2]", filters.getSubDomain());
    }

    private static void appendInPredicate(final StringBuilder sql,
                                           final List<Object[]> bindings,
                                           final String column,
                                           final List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        final String paramName = "p" + bindings.size();
        sql.append(" AND ").append(column).append(" = ANY(:").append(paramName).append(")");
        // Bind as a plain array (not a List) so Hibernate does NOT apply its automatic
        // multi-value "IN (:param)" -> "IN (?,?,?)" expansion, which would otherwise turn
        // "ANY(:param)" into the invalid "ANY(?,?,?)". A single array parameter binds to
        // exactly one "?" placeholder, as required by PostgreSQL's ANY(array) syntax.
        bindings.add(new Object[]{paramName, values.toArray(new String[0])});
    }

    private static void bindParameters(final Query query, final List<Object[]> bindings) {
        for (final Object[] binding : bindings) {
            query.setParameter((String) binding[0], binding[1]);
        }
    }

    private static Double toDouble(final Object value) {
        return value == null ? 0d : ((Number) value).doubleValue();
    }

    private static Long toLong(final Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    /**
     * Converts a native array result (JDBC {@link java.sql.Array}, {@code String[]}
     * or {@code List}, depending on driver/config) into a plain {@code List<String>}.
     */
    @SuppressWarnings("unchecked")
    private static List<String> toStringList(final Object value) {
        if (value == null) {
            return List.of();
        }
        try {
            if (value instanceof java.sql.Array sqlArray) {
                final Object[] array = (Object[]) sqlArray.getArray();
                final List<String> result = new ArrayList<>(array.length);
                for (final Object o : array) {
                    if (o != null) {
                        result.add(o.toString());
                    }
                }
                return result;
            }
            if (value instanceof List<?> list) {
                return (List<String>) list;
            }
            if (value instanceof String[] array) {
                return List.of(array);
            }
        } catch (final java.sql.SQLException e) {
            throw new IllegalStateException("Unable to read distinct filter values array", e);
        }
        return List.of();
    }

    /**
     * Simple POJO implementation of {@link MultiCriteriaImpactProjection}.
     */
    private record MultiCriteriaImpactProjectionImpl(String criterion, String unit, Double impact,
                                                       Double sip,
                                                       Long countValue) implements MultiCriteriaImpactProjection {
        @Override
        public String getCriterion() {
            return criterion;
        }

        @Override
        public String getUnit() {
            return unit;
        }

        @Override
        public Double getImpact() {
            return impact;
        }

        @Override
        public Double getSip() {
            return sip;
        }

        @Override
        public Long getCountValue() {
            return countValue;
        }
    }

    /**
     * Simple POJO implementation of {@link MultiCriteriaAggregateProjection}.
     */
    private record MultiCriteriaAggregateProjectionImpl(String criterion, String unit, String nodeLabel,
                                                          String repartitionLabel, Double impact, Double sip,
                                                          Long countValue, Long consistentCount,
                                                          Long inconsistentCount, Long subDomainCount,
                                                          Long applicationCount, String cluster,
                                                          String equipmentType,
                                                          String environment) implements MultiCriteriaAggregateProjection {
        @Override
        public String getCriterion() {
            return criterion;
        }

        @Override
        public String getUnit() {
            return unit;
        }

        @Override
        public String getNodeLabel() {
            return nodeLabel;
        }

        @Override
        public String getRepartitionLabel() {
            return repartitionLabel;
        }

        @Override
        public Double getImpact() {
            return impact;
        }

        @Override
        public Double getSip() {
            return sip;
        }

        @Override
        public Long getCountValue() {
            return countValue;
        }

        @Override
        public Long getConsistentCount() {
            return consistentCount;
        }

        @Override
        public Long getInconsistentCount() {
            return inconsistentCount;
        }

        @Override
        public Long getSubDomainCount() {
            return subDomainCount;
        }

        @Override
        public Long getApplicationCount() {
            return applicationCount;
        }

        @Override
        public String getCluster() {
            return cluster;
        }

        @Override
        public String getEquipmentType() {
            return equipmentType;
        }

        @Override
        public String getEnvironment() {
            return environment;
        }
    }

    /**
     * Simple POJO implementation of {@link HierarchyCountsProjection}.
     */
    private record HierarchyCountsProjectionImpl(Long domainCount, Long subDomainCount, Long applicationCount,
                                                  Long virtualEquipmentCount) implements HierarchyCountsProjection {
        @Override
        public Long getDomainCount() {
            return domainCount;
        }

        @Override
        public Long getSubDomainCount() {
            return subDomainCount;
        }

        @Override
        public Long getApplicationCount() {
            return applicationCount;
        }

        @Override
        public Long getVirtualEquipmentCount() {
            return virtualEquipmentCount;
        }
    }

    /**
     * Simple POJO implementation of {@link ApplicationDomainHierarchyProjection}.
     */
    private record ApplicationDomainHierarchyProjectionImpl(String domain,
                                                             List<String> subDomains) implements ApplicationDomainHierarchyProjection {
        @Override
        public String getDomain() {
            return domain;
        }

        @Override
        public List<String> getSubDomains() {
            return subDomains;
        }
    }

    /**
     * Simple POJO implementation of {@link ApplicationFiltersProjection}.
     */
    private record ApplicationFiltersProjectionImpl(List<String> environment, List<String> equipmentType,
                                                      List<String> lifeCycle,
                                                      List<ApplicationDomainHierarchyProjection> domains) implements ApplicationFiltersProjection {
        @Override
        public List<String> getEnvironment() {
            return environment;
        }

        @Override
        public List<String> getEquipmentType() {
            return equipmentType;
        }

        @Override
        public List<String> getLifeCycle() {
            return lifeCycle;
        }

        @Override
        public List<ApplicationDomainHierarchyProjection> getDomains() {
            return domains;
        }
    }

    /**
     * Simple POJO implementation of {@link VirtualEquipmentLowImpactCountProjection}.
     */
    private record VirtualEquipmentLowImpactCountProjectionImpl(Long totalCount,
                                                                  Long lowImpactCount) implements VirtualEquipmentLowImpactCountProjection {
        @Override
        public Long getTotalCount() {
            return totalCount;
        }

        @Override
        public Long getLowImpactCount() {
            return lowImpactCount;
        }
    }

    /**
     * Simple POJO implementation of {@link VirtualEquipmentElecConsumptionSumProjection}.
     */
    private record VirtualEquipmentElecConsumptionSumProjectionImpl(Double elecConsumptionSum,
                                                                      Long quantitySum,
                                                                      Long applicationCount) implements VirtualEquipmentElecConsumptionSumProjection {
        @Override
        public Double getElecConsumptionSum() {
            return elecConsumptionSum;
        }

        @Override
        public Long getQuantitySum() {
            return quantitySum;
        }

        @Override
        public Long getApplicationCount() {
            return applicationCount;
        }
    }
}






