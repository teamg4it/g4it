/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository.projection;

import java.util.List;

/**
 * §4.0 - distinct environment/equipmentType/lifeCycle values, plus the
 * domain -> subDomains hierarchy, for the given task, used to populate the
 * application view's filter selectors.
 */
public interface ApplicationFiltersProjection {

    List<String> getEnvironment();

    List<String> getEquipmentType();

    List<String> getLifeCycle();

    List<ApplicationDomainHierarchyProjection> getDomains();
}

