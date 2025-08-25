/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Organization Business Object.
 */
@Data
@SuperBuilder
public class WorkspaceBO {

    /**
     * The Organization's id.
     */
    private Long id;

    /**
     * The Organization's name.
     */
    private String name;

    /**
     * The 'default' flag.
     */
    private boolean defaultFlag;

    /**
     * The 'status'.
     */
    private String status;

    /**
     * The 'deletionDate'.
     */
    private LocalDateTime deletionDate;

    /**
     * The inventory criteria
     */
    private List<String> criteriaIs;

    /**
     * The digital service criteria
     */
    private List<String> criteriaDs;

    /**
     * User roles on organization.
     */
    private List<String> roles;

    private Long subscriberId;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;
}
