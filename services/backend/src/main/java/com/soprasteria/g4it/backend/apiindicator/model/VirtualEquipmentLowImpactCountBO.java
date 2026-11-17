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
 * Aggregated total and low-impact virtual equipment counts, filtered by the
 * shared dimensions. 100% database-side aggregation, replacing the client-side
 * reduce previously performed on the full virtualEquipmentsLowImpact list.
 */
@Data
@Builder
@NoArgsConstructor
@lombok.AllArgsConstructor
public class VirtualEquipmentLowImpactCountBO {

    private Long virtualEquipmentTotalCount;

    private Long lowImpactVirtualEquipmentCount;
}

