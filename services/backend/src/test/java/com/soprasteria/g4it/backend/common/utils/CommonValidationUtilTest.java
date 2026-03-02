/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class CommonValidationUtilTest {

    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;

    @Mock
    private BoaviztapiService boaviztapiService;

    @InjectMocks
    private CommonValidationUtil commonValidationUtil;

    @Test
    void validateCountry_returnsTrueWhenListContainsCountry() {
        when(digitalServiceReferentialService.getCountry()).thenReturn(List.of("France", "Spain"));
        assertTrue(commonValidationUtil.validateCountry("France"));
        verify(digitalServiceReferentialService).getCountry();
    }

    @Test
    void validateCountry_returnsFalseWhenListDoesNotContainCountry() {
        when(digitalServiceReferentialService.getCountry()).thenReturn(List.of("France", "Spain"));
        assertFalse(commonValidationUtil.validateCountry("Germany"));
        verify(digitalServiceReferentialService).getCountry();
    }

    @Test
    void validateBoaviztaCountry_returnsFalseWhenMapIsNull() {
        when(boaviztapiService.getCountryMap()).thenReturn(null);
        assertFalse(commonValidationUtil.validateboaviztaCountry("France"));
        verify(boaviztapiService).getCountryMap();
    }

    @Test
    void validateBoaviztaCountry_returnsFalseWhenMapIsEmpty() {
        when(boaviztapiService.getCountryMap()).thenReturn(Collections.emptyMap());
        assertFalse(commonValidationUtil.validateboaviztaCountry("France"));
        verify(boaviztapiService).getCountryMap();
    }

    @Test
    void validateBoaviztaCountry_returnsTrueWhenValueMatchesIgnoringCaseAndTrim() {
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("FR", "France", "ES", "Spain"));
        assertTrue(commonValidationUtil.validateboaviztaCountry("  frAnCe  "));
        verify(boaviztapiService).getCountryMap();
    }

    @Test
    void validateBoaviztaCountry_returnsFalseWhenNoValueMatches() {
        when(boaviztapiService.getCountryMap()).thenReturn(Map.of("FR", "France"));
        assertFalse(commonValidationUtil.validateboaviztaCountry("Germany"));
        verify(boaviztapiService).getCountryMap();
    }

    @Test
    void validateBoaviztaCountry_throwsNpeWhenCountryIsNull() {
        // method invokes country.trim() so a null input yields NPE
        assertThrows(NullPointerException.class, () -> commonValidationUtil.validateboaviztaCountry(null));
        verifyNoInteractions(boaviztapiService);
    }
}
