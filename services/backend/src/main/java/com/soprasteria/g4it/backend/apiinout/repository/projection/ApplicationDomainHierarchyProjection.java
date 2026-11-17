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
 * §4.0 - one domain and the distinct subDomains linked to it, for the given
 * task, used to populate the application view's domain/subDomain filter tree.
 */
public interface ApplicationDomainHierarchyProjection {

    String getDomain();

    List<String> getSubDomains();
}

