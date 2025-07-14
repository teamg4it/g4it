/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;


import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi.EvaluateBoaviztapiService;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.external.boavizta.model.response.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateBoaviztapiServiceTest {

    @InjectMocks
    private EvaluateBoaviztapiService evaluateBoaviztapiService;

    @Mock
    private BoaviztapiService boaviztapiService;

    @Test
    void evaluateReturnsImpactListWhenBoaviztapiServiceSucceeds() {
        InVirtualEquipment inVirtualEquipment = new InVirtualEquipment();
        List<String> criteria = List.of("CLIMATE_CHANGE");
        List<String> lifecycleSteps = List.of(Constants.MANUFACTURING, Constants.USING);

        BoaImpactRest impactRest = new BoaImpactRest();
        impactRest.setUnit("kgCO2e");
        impactRest.setEmbedded(BoaManufacturingRest.builder().value(100.0).build());
        impactRest.setUse(BoaUtilizationRest.builder().value(50.0).build());

        BoaResponseRest responseRest = new BoaResponseRest();
        responseRest.setImpacts(BoaImpactsRest.builder().gwp(impactRest).build());

        when(boaviztapiService.runBoaviztCalculations(inVirtualEquipment)).thenReturn(responseRest);

        List<ImpactBO> result = evaluateBoaviztapiService.evaluate(inVirtualEquipment, criteria, lifecycleSteps);

        assertEquals(2, result.size());
        assertEquals("CLIMATE_CHANGE", result.get(0).getCriterion());
        assertEquals(Constants.MANUFACTURING, result.get(0).getLifecycleStep());
        assertEquals(100.0, result.get(0).getUnitImpact());
        assertEquals("kgCO2e", result.get(0).getUnit());
        assertEquals("OK", result.get(0).getIndicatorStatus());
    }

    @Test
    void evaluateReturnsErrorListWhenBoaviztapiServiceThrowsException() {
        InVirtualEquipment inVirtualEquipment = new InVirtualEquipment();
        List<String> criteria = List.of("CLIMATE_CHANGE");
        List<String> lifecycleSteps = List.of(Constants.MANUFACTURING, Constants.USING);

        when(boaviztapiService.runBoaviztCalculations(inVirtualEquipment))
                .thenThrow(new ExternalApiException(500, "Internal Server Error"));

        List<ImpactBO> result = evaluateBoaviztapiService.evaluate(inVirtualEquipment, criteria, lifecycleSteps);

        assertEquals(2, result.size());
        assertEquals("CLIMATE_CHANGE", result.get(0).getCriterion());
        assertEquals(Constants.MANUFACTURING, result.get(0).getLifecycleStep());
        assertNull(result.get(0).getUnitImpact());
        assertNull(result.get(0).getUnit());
        assertEquals("ERROR", result.get(0).getIndicatorStatus());
        assertTrue(result.get(0).getTrace().contains("\"code\":500"));
        assertTrue(result.get(0).getTrace().contains("\"error\":\"Internal Server Error\""));
    }

    @Test
    void evaluateHandlesNullResponseFromBoaviztapiService() {
        InVirtualEquipment inVirtualEquipment = new InVirtualEquipment();
        List<String> criteria = List.of("CLIMATE_CHANGE");
        List<String> lifecycleSteps = List.of(Constants.MANUFACTURING, Constants.USING);

        when(boaviztapiService.runBoaviztCalculations(inVirtualEquipment)).thenReturn(null);

        List<ImpactBO> result = evaluateBoaviztapiService.evaluate(inVirtualEquipment, criteria, lifecycleSteps);

        assertEquals(2, result.size());
        assertEquals("CLIMATE_CHANGE", result.get(0).getCriterion());
        assertEquals(Constants.MANUFACTURING, result.get(0).getLifecycleStep());
        assertNull(result.get(0).getUnitImpact());
        assertNull(result.get(0).getUnit());
        assertEquals("ERROR", result.get(0).getIndicatorStatus());
    }

}
