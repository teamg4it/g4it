/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.model;

/**
 * The current position of the graph view in the drill-down tree.
 * <p>
 * {@code GLOBAL} is the initial state (no click yet) and performs NO node/hierarchy
 * grouping at all. From {@code DOMAIN} onward, the response contains one node per
 * value of the level.
 */
public enum GraphLevel {
    GLOBAL,
    DOMAIN,
    SUB_DOMAIN,
    APPLICATION,
    VIRTUAL_EQUIPMENT
}

