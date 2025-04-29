/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    DATACENTER("DATACENTER"),

    EQUIPEMENT_PHYSIQUE("EQUIPEMENT_PHYSIQUE"),

    EQUIPEMENT_VIRTUEL("EQUIPEMENT_VIRTUEL"),

    APPLICATION("APPLICATION"),

    INVENTORY("INVENTORY"),

    PHYSICAL_EQUIPMENT_INDICATOR("PHYSICAL_EQUIPMENT_INDICATOR"),

    VIRTUAL_EQUIPMENT("VIRTUAL_EQUIPMENT"),

    VIRTUAL_EQUIPMENT_INDICATOR("VIRTUAL_EQUIPMENT_INDICATOR"),

    APPLICATION_INDICATOR("APPLICATION_INDICATOR"),

    PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE("PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE"),

    VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE("VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE"),

    UNKNOWN("UNKNOWN");

    private String value;

    public static FileType fromValue(String value) {
        for (FileType b : FileType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public String getFileName() {
        return switch (this.getValue()) {
            case "DATACENTER" -> "datacenter";
            case "EQUIPEMENT_PHYSIQUE" -> "physical_equipment";
            case "EQUIPEMENT_VIRTUEL", "VIRTUAL_EQUIPMENT" -> "virtual_equipment";
            case "APPLICATION" -> "application";
            case "PHYSICAL_EQUIPMENT_INDICATOR", "PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE" -> "ind_physical_equipment";
            case "VIRTUAL_EQUIPMENT_INDICATOR", "VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE" -> "ind_virtual_equipment";
            case "APPLICATION_INDICATOR" -> "ind_application";
            default -> this.getValue();
        };
    }
}
