/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * §4.4 - paginated table view response wrapper (DB-side LIMIT/OFFSET, no
 * in-memory slicing).
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class ApplicationIndicatorsPageBO {

    private List<ApplicationIndicatorRowBO> content;

    private int pageNumber;

    private int pageSize;

    private long totalElements;

    private int totalPages;
}

