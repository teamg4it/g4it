package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.ecologits.EvaluateEcologitsService;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.ecologits.business.EcologitsService;
import com.soprasteria.g4it.backend.external.ecologits.model.response.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateEcologitsServiceTest {

    private static final List<String> LIFECYCLE_STEPS = List.of(
            "MANUFACTURING",
            "TRANSPORTATION",
            "USING",
            "END_OF_LIFE"
    );

    @Mock
    private EcologitsService ecologitsService;

    @InjectMocks
    private EvaluateEcologitsService evaluateEcologitsService;

    @Test
    void evaluate_mapsSupportedCriteriaAndUnsupportedCriteria() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .location("FRA")
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "FRA"))
                .thenReturn(responseWithImpacts());

        List<ImpactBO> impacts = evaluateEcologitsService.evaluate(
                aiService,
                List.of("CLIMATE_CHANGE", "RESOURCE_USE", "RESOURCE_USE_FOSSILS", "WATER_USE", "ACIDIFICATION"),
                LIFECYCLE_STEPS,
                Map.of()
        );

        assertEquals(20, impacts.size());

        ImpactBO climateManufacturing = findImpact(impacts, "CLIMATE_CHANGE", "MANUFACTURING");
        assertEquals("OK", climateManufacturing.getIndicatorStatus());
        assertEquals(7d, climateManufacturing.getUnitImpact());

        ImpactBO climateTransportation = findImpact(impacts, "CLIMATE_CHANGE", "TRANSPORTATION");
        assertEquals("KO", climateTransportation.getIndicatorStatus());
        assertEquals(0d, climateTransportation.getUnitImpact());
        assertTrue(climateTransportation.getTrace().contains("does not provide transportation impacts"));

        ImpactBO climateUsing = findImpact(impacts, "CLIMATE_CHANGE", "USING");
        assertEquals("OK", climateUsing.getIndicatorStatus());
        assertEquals(3d, climateUsing.getUnitImpact());

        ImpactBO climateEol =
                findImpact(impacts, "CLIMATE_CHANGE", "END_OF_LIFE");

        assertEquals("KO", climateEol.getIndicatorStatus());
        assertEquals(0d, climateEol.getUnitImpact());

        ImpactBO waterManufacturing = findImpact(impacts, "WATER_USE", "MANUFACTURING");
        assertEquals("OK", waterManufacturing.getIndicatorStatus());
        assertEquals(0d, waterManufacturing.getUnitImpact());

        ImpactBO unsupported = findImpact(impacts, "ACIDIFICATION", "USING");
        assertEquals("KO", unsupported.getIndicatorStatus());
        assertTrue(unsupported.getTrace().contains("does not provide this impact criterion"));
    }

    @Test
    void evaluate_usesWorldLocationWhenBlank() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(500L)
                .location(" ")
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 500, "WOR"))
                .thenReturn(responseWithImpacts());

        evaluateEcologitsService.evaluate(aiService, List.of("CLIMATE_CHANGE"), LIFECYCLE_STEPS, Map.of());

        verify(ecologitsService).runEstimation("openai", "gpt-4o-mini", 500, "WOR");
    }

    @Test
    void evaluate_returnsKoRowsWhenEcologitsThrows() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "WOR"))
                .thenThrow(new ExternalApiException(500, "boom"));

        List<ImpactBO> impacts = evaluateEcologitsService.evaluate(
                aiService,
                List.of("CLIMATE_CHANGE", "RESOURCE_USE"),
                LIFECYCLE_STEPS,
                Map.of()
        );

        assertEquals(8, impacts.size());
        assertTrue(impacts.stream().allMatch(impact -> "KO".equals(impact.getIndicatorStatus())));
        assertTrue(impacts.stream().allMatch(impact -> "boom".equals(impact.getTrace())));
    }

    @Test
    void evaluate_returnsKoRowsWhenEcologitsReturnsBusinessErrors() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .build();

        EcoImpactsRest impacts = new EcoImpactsRest();
        EcoStatusMessageRest error = new EcoStatusMessageRest();
        error.setCode("model-not-registered");
        error.setMessage("The model is not registered in the model repository.");
        impacts.setErrors(List.of(error));

        EcoEstimationResponseRest response = new EcoEstimationResponseRest();
        response.setImpacts(impacts);

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "WOR"))
                .thenReturn(response);

        List<ImpactBO> result = evaluateEcologitsService.evaluate(
                aiService,
                List.of("CLIMATE_CHANGE"),
                LIFECYCLE_STEPS,
                Map.of()
        );

        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(impact -> "KO".equals(impact.getIndicatorStatus())));
        assertTrue(result.getFirst().getTrace().contains("model-not-registered"));
    }

    @Test
    void evaluate_filtersToActiveCriteriaOnly() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "WOR"))
                .thenReturn(responseWithImpacts());

        List<ImpactBO> impacts = evaluateEcologitsService.evaluate(
                aiService,
                List.of("RESOURCE_USE"),
                LIFECYCLE_STEPS,
                Map.of()
        );

        assertEquals(4, impacts.size());
        assertTrue(impacts.stream().allMatch(impact -> "RESOURCE_USE".equals(impact.getCriterion())));
    }

    @Test
    void evaluate_resolvesCountryNameToIsoCodeUsingCountryMap() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .location("France")
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "FRA"))
                .thenReturn(responseWithImpacts());

        evaluateEcologitsService.evaluate(
                aiService,
                List.of("CLIMATE_CHANGE"),
                LIFECYCLE_STEPS,
                Map.of("france", "FRA", "india", "IND")
        );

        verify(ecologitsService).runEstimation("openai", "gpt-4o-mini", 1000, "FRA");
    }

    @Test
    void evaluate_passesThroughUnknownLocationWhenNotInCountryMap() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .location("FRA")
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "FRA"))
                .thenReturn(responseWithImpacts());

        evaluateEcologitsService.evaluate(
                aiService,
                List.of("CLIMATE_CHANGE"),
                LIFECYCLE_STEPS,
                Map.of("france", "FRA", "india", "IND")
        );

        verify(ecologitsService).runEstimation("openai", "gpt-4o-mini", 1000, "FRA");
    }

    @Test
    void evaluate_mapsTransportationAndEndOfLifeToDataInconsistency() {
        InAiService aiService = InAiService.builder()
                .serviceName("Assistant")
                .provider("openai")
                .model("gpt-4o-mini")
                .outputTokens(1000L)
                .location("FRA")
                .build();

        when(ecologitsService.runEstimation("openai", "gpt-4o-mini", 1000, "FRA"))
                .thenReturn(responseWithImpacts());

        List<ImpactBO> impacts = evaluateEcologitsService.evaluate(
                aiService,
                List.of("CLIMATE_CHANGE"),
                LIFECYCLE_STEPS,
                Map.of()
        );

        ImpactBO transportation = findImpact(impacts, "CLIMATE_CHANGE", "TRANSPORTATION");
        assertEquals("KO", transportation.getIndicatorStatus());
        assertEquals(0d, transportation.getUnitImpact());
        assertTrue(transportation.getTrace().contains("does not provide transportation impacts"));

        ImpactBO endOfLife = findImpact(impacts, "CLIMATE_CHANGE", "END_OF_LIFE");
        assertEquals("KO", endOfLife.getIndicatorStatus());
        assertEquals(0d, endOfLife.getUnitImpact());
        assertTrue(endOfLife.getTrace().contains("does not provide end-of-life impacts"));
    }

    private EcoEstimationResponseRest responseWithImpacts() {
        EcoImpactPhaseRest usage = new EcoImpactPhaseRest();
        usage.setGwp(metric("GWP", "kgCO2eq", 2d, 4d));
        usage.setAdpe(metric("ADPe", "kgSbeq", 1d, 3d));
        usage.setPe(metric("PE", "MJ", 10d, 14d));
        usage.setWcf(metric("WCF", "L", 100d, 200d));

        EcoImpactPhaseRest embodied = new EcoImpactPhaseRest();
        embodied.setGwp(metric("GWP", "kgCO2eq", 6d, 8d));
        embodied.setAdpe(metric("ADPe", "kgSbeq", 5d, 7d));
        embodied.setPe(metric("PE", "MJ", 20d, 24d));

        EcoImpactsRest impacts = new EcoImpactsRest();
        impacts.setGwp(metric("GWP", "kgCO2eq", 8d, 12d));
        impacts.setAdpe(metric("ADPe", "kgSbeq", 6d, 10d));
        impacts.setPe(metric("PE", "MJ", 30d, 38d));
        impacts.setWcf(metric("WCF", "L", 100d, 200d));
        impacts.setUsage(usage);
        impacts.setEmbodied(embodied);

        EcoEstimationResponseRest response = new EcoEstimationResponseRest();
        response.setImpacts(impacts);
        return response;
    }

    private EcoMetricRest metric(String type, String unit, double min, double max) {
        EcoValueRangeRest value = new EcoValueRangeRest();
        value.setMin(min);
        value.setMax(max);

        EcoMetricRest metric = new EcoMetricRest();
        metric.setType(type);
        metric.setUnit(unit);
        metric.setValue(value);
        return metric;
    }

    private ImpactBO findImpact(List<ImpactBO> impacts, String criterion, String lifecycleStep) {
        return impacts.stream()
                .filter(impact -> criterion.equals(impact.getCriterion()) && lifecycleStep.equals(impact.getLifecycleStep()))
                .findFirst()
                .orElseThrow();
    }
}