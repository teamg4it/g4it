/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class HostUtilsTest {

    @Test
    void constructorShouldThrowException() throws Exception {
        Constructor<HostUtils> constructor = HostUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(
                UnsupportedOperationException.class,
                exception.getCause());

        assertEquals(
                "This is a utility class and cannot be instantiated",
                exception.getCause().getMessage());
    }

    @Test
    void shouldReturnGoodWhenQuartileAndPueAreGood() {
        assertEquals(
                "Good",
                HostUtils.buildHostingEfficiency(1, 1.4));
    }

    @Test
    void shouldReturnMediumWhenQuartileIsMediumAndPueIsGood() {
        assertEquals(
                "Medium",
                HostUtils.buildHostingEfficiency(2, 1.4));
    }

    @Test
    void shouldReturnMediumWhenQuartileIsGoodAndPueIsMedium() {
        assertEquals(
                "Medium",
                HostUtils.buildHostingEfficiency(1, 2.0));
    }

    @Test
    void shouldReturnBadWhenQuartileIsBadAndPueIsGood() {
        assertEquals(
                "Bad",
                HostUtils.buildHostingEfficiency(4, 1.4));
    }

    @Test
    void shouldReturnBadWhenQuartileIsGoodAndPueIsBad() {
        assertEquals(
                "Bad",
                HostUtils.buildHostingEfficiency(1, 3.0));
    }

    @Test
    void shouldReturnMediumForQuartileThree() {
        assertEquals(
                "Medium",
                HostUtils.buildHostingEfficiency(3, 1.4));
    }

    @Test
    void shouldReturnBadForQuartileGreaterThanThree() {
        assertEquals(
                "Bad",
                HostUtils.buildHostingEfficiency(5, 1.4));
    }

    @Test
    void shouldReturnMediumWhenPueEqualsOnePointFive() {
        assertEquals(
                "Medium",
                HostUtils.buildHostingEfficiency(1, 1.5));
    }

    @Test
    void shouldReturnMediumWhenPueEqualsTwoPointFive() {
        assertEquals(
                "Medium",
                HostUtils.buildHostingEfficiency(1, 2.5));
    }

    @Test
    void shouldReturnBadWhenPueGreaterThanTwoPointFive() {
        assertEquals(
                "Bad",
                HostUtils.buildHostingEfficiency(1, 2.6));
    }
}
