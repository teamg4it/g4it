/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationDomainHierarchyProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationFiltersProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.HierarchyCountsProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaAggregateProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaImpactProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentElecConsumptionSumProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.VirtualEquipmentLowImpactCountProjection;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application indicator mapper.
 */
@Mapper(componentModel = "spring")
public interface ApplicationIndicatorMapper {

    List<ApplicationImpactBO> toOutImpact(final List<OutApplication> source);

    @Mapping(target = "lifeCycle", source = "lifecycleStep")
    @Mapping(target = "environment", source = "environment")
    @Mapping(target = "applicationName", source = "name")
    @Mapping(target = "impact", source = "unitImpact")
    @Mapping(target = "sip", source = "peopleEqImpact")
    @Mapping(target = "statusIndicator", source = "statusIndicator")
    @Mapping(target = "domain", expression = "java(java.util.Optional.ofNullable(source.getFilters().get(0)).filter(s -> !s.isEmpty()).orElse(\"Unknown\"))")
    @Mapping(target = "subDomain",
            expression = "java(java.util.Optional.ofNullable(source.getFilters().get(1)).filter(s -> !s.isEmpty()).orElse(\"Unknown\"))")
    @Mapping(target = "cluster", expression = "java(source.getFiltersVirtualEquipment().get(0))")
    @Mapping(target = "location", source = "location")
    ApplicationImpactBO toOutImpact(final OutApplication source);

    @Mapping(target = "impacts", source = "source")
    default List<ApplicationIndicatorBO<ApplicationImpactBO>> toOutDto(final List<OutApplication> source) {
        return source.stream().collect(Collectors.groupingBy(ind -> Pair.of(ind.getCriterion(), ind.getUnit())))
                .entrySet().stream().
                        <ApplicationIndicatorBO<ApplicationImpactBO>>map(entry -> ApplicationIndicatorBO.builder()
                        .criteria(StringUtils.snakeToKebabCase(entry.getKey().getKey()))
                        .unit(entry.getKey().getValue())
                        .impacts(toOutImpact(entry.getValue()))
                        .build())
                .toList();
    }

    /**
     * §4.1 - maps the (applicationName-less) request body into the shared filter BO.
     */
    ApplicationCriteriaFilterBO toFilterBO(final com.soprasteria.g4it.backend.server.gen.api.dto.ApplicationMultiCriteriaImpactsRequestRest source);

    /**
     * §4.2/§4.3/§4.3-counts - maps the shared filter REST object into the BO.
     */
    ApplicationCriteriaFilterBO toFilterBO(final com.soprasteria.g4it.backend.server.gen.api.dto.ApplicationCriteriaFilterRest source);

    /**
     * §4.1 - pure 1:1 mapping of already-aggregated projection rows (one per
     * criterion) into BOs. No grouping/summing happens here.
     */
    default List<ApplicationMultiCriteriaImpactBO> toMultiCriteriaImpactBO(final List<MultiCriteriaImpactProjection> source) {
        return source.stream()
                .map(p -> ApplicationMultiCriteriaImpactBO.builder()
                        .criteria(StringUtils.snakeToKebabCase(p.getCriterion()))
                        .unit(p.getUnit())
                        .impact(p.getImpact())
                        .sip(p.getSip())
                        .countValue(p.getCountValue())
                        .build())
                .toList();
    }

    /**
     * §4.2/§4.3 - re-nests the already-aggregated projection rows (one per
     * criterion x nodeLabel x repartitionLabel) into
     * {@code criteria -> nodes[] -> repartitions[]}. This groups already-summed
     * numbers only to reshape them - it never recomputes a sum/count.
     */
    default List<ApplicationMultiCriteriaBO> toMultiCriteriaBO(final List<MultiCriteriaAggregateProjection> source) {
        // Collectors.groupingBy() throws NPE on a null key (e.g. nodeLabel is always
        // null for GraphLevel.GLOBAL, since there is no hierarchy grouping). Use a
        // manual LinkedHashMap grouping instead, which supports null keys.
        final Map<Pair<String, String>, List<MultiCriteriaAggregateProjection>> byCriteria = new LinkedHashMap<>();
        for (final MultiCriteriaAggregateProjection p : source) {
            byCriteria.computeIfAbsent(Pair.of(p.getCriterion(), p.getUnit()), k -> new ArrayList<>()).add(p);
        }

        return byCriteria.entrySet().stream()
                .map(criteriaEntry -> {
                    final Map<String, List<MultiCriteriaAggregateProjection>> byNode = new LinkedHashMap<>();
                    for (final MultiCriteriaAggregateProjection p : criteriaEntry.getValue()) {
                        byNode.computeIfAbsent(p.getNodeLabel(), k -> new ArrayList<>()).add(p);
                    }
                    final List<ApplicationNodeBO> nodes = byNode.entrySet().stream()
                            .map(nodeEntry -> toNodeBO(nodeEntry.getKey(), nodeEntry.getValue()))
                            .toList();
                    return ApplicationMultiCriteriaBO.builder()
                            .criteria(StringUtils.snakeToKebabCase(criteriaEntry.getKey().getKey()))
                            .unit(criteriaEntry.getKey().getValue())
                            .nodes(nodes)
                            .build();
                })
                .toList();
    }

    /**
     * Builds one {@link ApplicationNodeBO} from all its repartition rows,
     * summing the level-specific extra fields (already a single value per node
     * since the DB grouped by nodeLabel too - {@code MIN}/{@code first} is used
     * to fetch them, never recomputed).
     */
    private ApplicationNodeBO toNodeBO(final String nodeLabel, final List<MultiCriteriaAggregateProjection> rows) {
        final MultiCriteriaAggregateProjection first = rows.get(0);
        final List<ApplicationRepartitionImpactBO> repartitions = rows.stream()
                .map(r -> ApplicationRepartitionImpactBO.builder()
                        .label(r.getRepartitionLabel())
                        .impact(r.getImpact())
                        .sip(r.getSip())
                        .countValue(r.getCountValue())
                        .consistentCount(r.getConsistentCount())
                        .inconsistentCount(r.getInconsistentCount())
                        .build())
                .toList();

        return ApplicationNodeBO.builder()
                .label(nodeLabel)
                .impact(repartitions.stream().mapToDouble(r -> r.getImpact() == null ? 0d : r.getImpact()).sum())
                .sip(repartitions.stream().mapToDouble(r -> r.getSip() == null ? 0d : r.getSip()).sum())
                .countValue(repartitions.stream().mapToLong(r -> r.getCountValue() == null ? 0L : r.getCountValue()).sum())
                .consistentCount(repartitions.stream().mapToLong(r -> r.getConsistentCount() == null ? 0L : r.getConsistentCount()).sum())
                .inconsistentCount(repartitions.stream().mapToLong(r -> r.getInconsistentCount() == null ? 0L : r.getInconsistentCount()).sum())
                .subDomainCount(first.getSubDomainCount())
                .applicationCount(first.getApplicationCount())
                .cluster(first.getCluster())
                .equipmentType(first.getEquipmentType())
                .environment(first.getEnvironment())
                .repartitions(repartitions)
                .build();
    }

    /**
     * §4.3 (optional) - pure 1:1 mapping of the single aggregated counts row.
     */
    default ApplicationHierarchyCountsBO toHierarchyCountsBO(final HierarchyCountsProjection source) {
        return ApplicationHierarchyCountsBO.builder()
                .domainCount(source.getDomainCount())
                .subDomainCount(source.getSubDomainCount())
                .applicationCount(source.getApplicationCount())
                .virtualEquipmentCount(source.getVirtualEquipmentCount())
                .build();
    }

    /**
     * Pure 1:1 mapping of the single aggregated virtual equipment low-impact
     * counts row (total count / low-impact count).
     */
    default VirtualEquipmentLowImpactCountBO toVirtualEquipmentLowImpactCountBO(final VirtualEquipmentLowImpactCountProjection source) {
        return VirtualEquipmentLowImpactCountBO.builder()
                .virtualEquipmentTotalCount(source.getTotalCount())
                .lowImpactVirtualEquipmentCount(source.getLowImpactCount())
                .build();
    }

    /**
     * Pure 1:1 mapping of the single aggregated virtual equipment electricity
     * consumption sum row (elecConsumption sum / quantity sum).
     */
    default VirtualEquipmentElecConsumptionSumBO toVirtualEquipmentElecConsumptionSumBO(final VirtualEquipmentElecConsumptionSumProjection source) {
        return VirtualEquipmentElecConsumptionSumBO.builder()
                .elecConsumptionSum(source.getElecConsumptionSum())
                .noOfVirtualEquipmentsSum(source.getQuantitySum())
                .applicationCount(source.getApplicationCount())
                .build();
    }

    /**
     * §4.4 - maps one flattened row of the paginated table view.
     */
    @Mapping(target = "criteria", expression = "java(com.soprasteria.g4it.backend.common.utils.StringUtils.snakeToKebabCase(source.getCriterion()))")
    @Mapping(target = "lifeCycle", source = "lifecycleStep")
    @Mapping(target = "applicationName", source = "name")
    @Mapping(target = "impact", source = "unitImpact")
    @Mapping(target = "sip", source = "peopleEqImpact")
    @Mapping(target = "domain", expression = "java(java.util.Optional.ofNullable(source.getFilters().get(0)).filter(s -> !s.isEmpty()).orElse(\"Unknown\"))")
    @Mapping(target = "subDomain", expression = "java(java.util.Optional.ofNullable(source.getFilters().get(1)).filter(s -> !s.isEmpty()).orElse(\"Unknown\"))")
    @Mapping(target = "cluster", expression = "java(source.getFiltersVirtualEquipment().get(0))")
    ApplicationIndicatorRowBO toRowBO(final OutApplication source);

    List<ApplicationIndicatorRowBO> toRowBO(final List<OutApplication> source);

    /**
     * §4.0 - pure 1:1 copy from the DB projection (already distinct/aggregated) to the BO.
     */
    default ApplicationFiltersBO toFiltersBO(final ApplicationFiltersProjection source) {
        if (source == null) {
            return ApplicationFiltersBO.builder()
                    .environment(List.of())
                    .equipmentType(List.of())
                    .lifeCycle(List.of())
                    .domains(List.of())
                    .build();
        }
        return ApplicationFiltersBO.builder()
                .environment(source.getEnvironment())
                .equipmentType(source.getEquipmentType())
                .lifeCycle(source.getLifeCycle())
                .domains(toDomainHierarchyBO(source.getDomains()))
                .build();
    }

    /**
     * §4.0 - maps each domain's linked distinct subDomains (already grouped
     * DB-side) into the BO tree; no in-memory grouping/aggregation performed.
     */
    default List<ApplicationDomainHierarchyBO> toDomainHierarchyBO(final List<ApplicationDomainHierarchyProjection> source) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(d -> ApplicationDomainHierarchyBO.builder()
                        .domain(d.getDomain())
                        .subDomains(d.getSubDomains())
                        .build())
                .collect(Collectors.toList());
    }

}


