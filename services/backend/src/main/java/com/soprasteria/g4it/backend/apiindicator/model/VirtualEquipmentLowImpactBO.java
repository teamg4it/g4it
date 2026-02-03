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

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualEquipmentLowImpactBO implements Serializable {

    private Long inventoryId;

    private String inventoryName;

    private String country;

    private String infrastructureType;

    private String provider;

    private String lifecycleStep;

    private String nomEntite;

    private String domain;

    private String subDomain;

    private String environment;

    private String equipmentType;

    private Double quantity;

    private Boolean lowImpact;


}

