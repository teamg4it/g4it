package com.soprasteria.g4it.backend.external.boavizta.business;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.external.boavizta.client.BoaviztapiClient;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoaviztapiServiceTest {

    @Mock
    private BoaviztapiClient boaviztapiClient;

    @InjectMocks
    private BoaviztapiService boaviztapiService;

    private InVirtualEquipment virtualEquipment;

    @BeforeEach
    void setUp() {
        virtualEquipment = new InVirtualEquipment();
    }

    @Test
    void getCountryMap_shouldReturnClientResult() {
        Map<String, String> mockMap = Map.of("GBR", "United Kingdom");
        when(boaviztapiClient.getAllCountries()).thenReturn(mockMap);

        Map<String, String> result = boaviztapiService.getCountryMap();

        assertThat(result).isEqualTo(mockMap);
        verify(boaviztapiClient).getAllCountries();
    }

    @Test
    void getProviderList_shouldReturnClientResult() {
        List<String> providers = List.of("azure", "aws");
        when(boaviztapiClient.getAllProviders()).thenReturn(providers);

        List<String> result = boaviztapiService.getProviderList();

        assertThat(result).containsExactly("azure", "aws");
        verify(boaviztapiClient).getAllProviders();
    }

    @Test
    void getInstanceList_shouldReturnClientResult() {
        List<String> instances = List.of("d2ads_v5", "d16ads_v5");
        when(boaviztapiClient.getAllInstances("azure")).thenReturn(instances);

        List<String> result = boaviztapiService.getInstanceList("azure");

        assertThat(result).containsExactly("d2ads_v5", "d16ads_v5");
        verify(boaviztapiClient).getAllInstances("azure");
    }

    @Test
    void runBoaviztCalculations_shouldDelegateToClient() {
        BoaResponseRest response = new BoaResponseRest();
        when(boaviztapiClient.runCalculation(virtualEquipment)).thenReturn(response);

        BoaResponseRest result = boaviztapiService.runBoaviztCalculations(virtualEquipment);

        assertThat(result).isSameAs(response);
        verify(boaviztapiClient).runCalculation(virtualEquipment);
    }

    @Test
    void computeAnnualElectricityKwhRaw_shouldReturnCorrectValue() {
        // 100 W * 8760h / 1000 * 2 = 1752 kWh
        double result = boaviztapiService.computeAnnualElectricityKwhRaw(
                100d,
                8760d,
                2d
        );

        assertThat(result).isEqualTo(1752d);
    }

    @Test
    void computeAnnualElectricityKwhRaw_shouldReturnZeroIfAvgPowerNull() {
        double result = boaviztapiService.computeAnnualElectricityKwhRaw(
                null,
                8760d,
                2d
        );

        assertThat(result).isZero();
    }

    @Test
    void extractAvgPowerW_shouldReturnValueWhenPresent() {
        BoaResponseRest response = mock(BoaResponseRest.class, RETURNS_DEEP_STUBS);
        when(response.getVerbose().getAvgPower().getValue()).thenReturn(7.5);

        Optional<Double> result = boaviztapiService.extractAvgPowerW(response);

        assertThat(result)
                .isPresent()
                .contains(7.5);

    }

    @Test
    void extractAvgPowerW_shouldReturnEmptyWhenResponseNull() {
        Optional<Double> result = boaviztapiService.extractAvgPowerW(null);

        assertThat(result).isEmpty();
    }

    @Test
    void extractAvgPowerW_shouldReturnEmptyWhenVerboseNull() {
        BoaResponseRest response = mock(BoaResponseRest.class);
        when(response.getVerbose()).thenReturn(null);

        Optional<Double> result = boaviztapiService.extractAvgPowerW(response);

        assertThat(result).isEmpty();
    }

    @Test
    void extractAvgPowerW_shouldReturnEmptyWhenAvgPowerNull() {
        BoaResponseRest response = mock(BoaResponseRest.class, RETURNS_DEEP_STUBS);
        when(response.getVerbose().getAvgPower()).thenReturn(null);

        Optional<Double> result = boaviztapiService.extractAvgPowerW(response);

        assertThat(result).isEmpty();
    }

    @Test
    void extractAvgPowerW_shouldReturnEmptyWhenValueNull() {
        BoaResponseRest response = mock(BoaResponseRest.class, RETURNS_DEEP_STUBS);
        when(response.getVerbose().getAvgPower().getValue()).thenReturn(null);

        Optional<Double> result = boaviztapiService.extractAvgPowerW(response);

        assertThat(result).isEmpty();
    }
}
