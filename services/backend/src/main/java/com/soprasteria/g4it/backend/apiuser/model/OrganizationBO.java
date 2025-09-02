/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Subscriber Business Object.
 */
@Data
@SuperBuilder
public class OrganizationBO {

    /**
     * The subscriber's id.
     */
    private Long id;

    /**
     * The subscriber's name.
     */
    private String name;

    /**
     * The 'default' flag.
     */
    private boolean defaultFlag;

    /**
     * Subscriber's organization.
     */
    private List<WorkspaceBO> workspaces;

    /**
     * The authorized organizations to subscriber
     */
    private String authorizedDomains;

    /**
     * The criteria
     */
    private List<String> criteria;

    /**
     * User roles on subscriber.
     */
    private List<String> roles;
    /**
     * Is EcomindAi enabled or not for this subscriber
     */
    private boolean ecomindai;

}
