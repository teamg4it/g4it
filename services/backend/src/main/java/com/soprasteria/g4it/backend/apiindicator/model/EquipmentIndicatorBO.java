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

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EquipmentIndicatorBO {

    private String label;

    private String unit;

    private List<EquipmentImpactBO> impacts;

    /** Unrounded quantity share among evaluated Devices, or null when unavailable. */
    private Double byodDevicesPercentage;

    /** Unrounded quantity share among evaluated Devices, or null when unavailable. */
    private Double copeDevicesPercentage;
}
