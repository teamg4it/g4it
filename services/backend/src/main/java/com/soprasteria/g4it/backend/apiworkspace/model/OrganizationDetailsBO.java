/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class OrganizationDetailsBO {
    /**
     * The Organization's id.
     */
    private Long id;

    /**
     * The Organization's name.
     */
    private String name;

    /**
     * The 'status'.
     */
    private String status;
}
