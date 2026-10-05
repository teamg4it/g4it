/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.mapper;

import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Objects;

/**
 * in application mapper.
 */
@Mapper(componentModel = "spring")
public interface InApplicationMapper {

    List<InApplicationRest> toRest(final List<InApplication> source);

    InApplicationRest toRest(final InApplication source);

    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    InApplication toEntity(final InApplicationRest source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", expression = "java(java.time.LocalDateTime.now())")
    void merge(@MappingTarget final InApplication target, final InApplication source);

    @AfterMapping
    default void normalizeFilters(@MappingTarget final InApplication target) {
        target.setCommonFilters(nullIfEmptyOrBlank(target.getCommonFilters()));
        target.setFilters(nullIfEmptyOrBlank(target.getFilters()));
    }

    private static List<String> nullIfEmptyOrBlank(final List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of("");
        }
        final List<String> normalized = values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
        return normalized.isEmpty() ? List.of("") : normalized;
    }
}
