/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository.projection;

/**
 * JPA projection for the aggregated virtual equipment low-impact counts
 * endpoint (total count / low-impact count), 100% database-side aggregation.
 */
public interface VirtualEquipmentLowImpactCountProjection {

    Long getTotalCount();

    Long getLowImpactCount();
}

