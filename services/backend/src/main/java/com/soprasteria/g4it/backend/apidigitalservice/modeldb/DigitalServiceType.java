/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

public enum DigitalServiceType {

    SHARED_SERVER("Shared Server"),
    DEDICATED_SERVER("Dedicated Server"),
    TERMINAL("Terminal"),
    NETWORK("Network");

    private final String value;

    DigitalServiceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Utility method to check if a given type is valid
    public static boolean isValid(String type) {
        for (DigitalServiceType serviceType : DigitalServiceType.values()) {
            if (serviceType.getValue().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
