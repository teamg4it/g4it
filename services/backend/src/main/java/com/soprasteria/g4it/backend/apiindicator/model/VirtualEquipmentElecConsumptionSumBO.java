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

/**
 * Aggregated electricity consumption sum and virtual equipment quantity sum,
 * filtered by the shared dimensions. 100% database-side aggregation, replacing
 * the client-side reduce previously performed on the full
 * virtualEquipmentsElecConsumption list.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class VirtualEquipmentElecConsumptionSumBO {

    private Double elecConsumptionSum;

    private Long noOfVirtualEquipmentsSum;

    private Long applicationCount;
}

