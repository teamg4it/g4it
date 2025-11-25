/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

public enum DigitalServiceVersionStatus {

    ACTIVE("active"),
    ARCHIVED("archived"),
    DRAFT("draft");

    private final String value;

    DigitalServiceVersionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
