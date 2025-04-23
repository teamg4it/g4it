/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

public class DoubleUtils {

    private DoubleUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Double toDouble(Integer value) {
        if (value == null) return null;
        return Double.valueOf(value);
    }

    public static Double toDouble(String value) {
        return toDouble(value, null);
    }

    public static Double toDouble(String value, Double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        return Double.parseDouble(value);
    }

}
