/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.mapper;

import com.soprasteria.g4it.backend.apiindicator.model.ApplicationImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationIndicatorBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
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

}
