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

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VirtualEquipmentElecConsumptionBO {

    private String name;
    private String location;
    private String lifeCycle;
    private String domain;
    private String subDomain;
    private String environment;
    private String equipmentType;
    private Double elecConsumption;
    private Integer quantity;
}
