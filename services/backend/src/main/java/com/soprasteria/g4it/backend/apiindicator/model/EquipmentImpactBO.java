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
public class EquipmentImpactBO {

    private String acvStep;

    private String country;

    private String entity;

    private String equipment;

    private String status;

    private Double impact;

    private Double sip;

    private Double quantity;

    private String statusIndicator;

    private Long countValue;

    private String level;
}
