/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceReferentialCloudTest {

    @Mock
    private BoaviztapiService boaviztapiService;

    @InjectMocks
    private DigitalServiceReferentialService digitalServiceReferentialService;

    @Test
    void shouldGetBoaviztaCountryMap() {
        Map<String, String> expectedCountryMap = Map.of("FR", "France", "US", "United States");
        when(boaviztapiService.getCountryMap()).thenReturn(expectedCountryMap);

        Map<String, String> result = digitalServiceReferentialService.getBoaviztaCountryMap();

        assertThat(result).isEqualTo(expectedCountryMap);
        verify(boaviztapiService, times(1)).getCountryMap();
    }

    @Test
    void shouldGetCloudProviders() {
        List<String> expectedProviders = List.of("AWS", "Azure", "Google Cloud");
        when(boaviztapiService.getProviderList()).thenReturn(expectedProviders);

        List<String> result = digitalServiceReferentialService.getCloudProviders();

        assertThat(result).isEqualTo(expectedProviders);
        verify(boaviztapiService, times(1)).getProviderList();
    }

    @Test
    void shouldGetCloudInstances_forValidProvider() {
        String providerName = "AWS";
        List<String> expectedInstances = List.of("t2.micro", "t2.large");
        when(boaviztapiService.getInstanceList(providerName)).thenReturn(expectedInstances);

        List<String> result = digitalServiceReferentialService.getCloudInstances(providerName);

        assertThat(result).isEqualTo(expectedInstances);
        verify(boaviztapiService, times(1)).getInstanceList(providerName);
    }

    @Test
    void shouldGetCloudInstances_forInvalidProvider() {
        String providerName = "InvalidProvider";
        when(boaviztapiService.getInstanceList(providerName)).thenReturn(List.of());

        List<String> result = digitalServiceReferentialService.getCloudInstances(providerName);

        assertThat(result).isEmpty();
        verify(boaviztapiService, times(1)).getInstanceList(providerName);
    }
}

