/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

/**
 * The fixed, flat secondary breakdown axis used by the application graph view,
 * independent of the current {@link GraphLevel} drill position.
 */
public enum RepartitionType {
    LIFE_CYCLE,
    ENVIRONMENT,
    EQUIPMENT_TYPE
}

