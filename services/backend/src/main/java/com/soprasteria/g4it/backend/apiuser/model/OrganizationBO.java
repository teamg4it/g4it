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
 * organization Business Object.
 */
@Data
@SuperBuilder
public class OrganizationBO {

    /**
     * The organization's id.
     */
    private Long id;

    /**
     * The organization's name.
     */
    private String name;

    /**
     * The 'default' flag.
     */
    private boolean defaultFlag;

    /**
     * organization's workspace.
     */
    private List<WorkspaceBO> workspaces;

    /**
     * The authorized domains to organization
     */
    private String authorizedDomains;

    /**
     * The criteria
     */
    private List<String> criteria;

    /**
     * User roles on organization.
     */
    private List<String> roles;
    /**
     * Is EcomindAi enabled or not for this organization
     */
    private boolean ecomindai;

}
