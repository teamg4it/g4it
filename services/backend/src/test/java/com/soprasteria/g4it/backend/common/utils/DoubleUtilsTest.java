/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleUtilsTest {

    @Test
    void toDoubleWithIntegerReturnsDoubleValue() {
        assertEquals(10.0, DoubleUtils.toDouble(10));
    }

    @Test
    void toDoubleWithNullIntegerReturnsNull() {
        assertNull(DoubleUtils.toDouble((Integer) null));
    }

    @Test
    void toDoubleWithStringReturnsDoubleValue() {
        assertEquals(10.5, DoubleUtils.toDouble("10.5"));
    }

    @Test
    void toDoubleWithNullStringReturnsDefaultValue() {
        assertEquals(5.0, DoubleUtils.toDouble(null, 5.0));
    }

    @Test
    void toDoubleWithEmptyStringReturnsDefaultValue() {
        assertEquals(5.0, DoubleUtils.toDouble("", 5.0));
    }

    @Test
    void toDoubleWithInvalidStringThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> DoubleUtils.toDouble("invalid"));
    }

}