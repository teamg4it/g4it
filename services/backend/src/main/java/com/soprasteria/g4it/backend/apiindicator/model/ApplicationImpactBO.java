/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Application indicators impacts business object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApplicationImpactBO {

    /**
     * The acv step (life cycle)
     */
    private String lifeCycle;

    /**
     * The application's domain.
     */
    private String domain;

    /**
     * The application's subDomain.
     */
    private String subDomain;

    /**
     * The environment.
     */
    private String environment;

    /**
     * The linked equipment type.
     */
    private String equipmentType;

    /**
     * The application name.
     */
    private String applicationName;

    /**
     * The virtual equipment name.
     */
    private String virtualEquipmentName;

    /**
     * The cluster.
     */
    private String cluster;

    /**
     * Application impact.
     */
    private Double impact;

    /**
     * Application impact (in people equivalent).
     */
    private Double sip;

    /**
     * The status of the indicator(OK or ERREUR)
     */
    private String statusIndicator;

    private String provider;

    private String location;

}
