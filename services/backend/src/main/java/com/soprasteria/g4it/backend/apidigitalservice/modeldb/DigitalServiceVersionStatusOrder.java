/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

public enum DigitalServiceVersionStatusOrder {
    ACTIVE(1),
    DRAFT(2),
    ARCHIVED(3);

    private final int sortOrder;

    DigitalServiceVersionStatusOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public static DigitalServiceVersionStatusOrder from(String value) {
        return DigitalServiceVersionStatusOrder.valueOf(value.toUpperCase());
    }
}
