/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository.projection;

/**
 * JPA projection for the §4.3 optional standing hierarchy counts endpoint.
 */
public interface HierarchyCountsProjection {

    Long getDomainCount();

    Long getSubDomainCount();

    Long getApplicationCount();

    Long getVirtualEquipmentCount();
}

