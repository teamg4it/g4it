/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.ecologits;

import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.ecologits.business.EcologitsService;
import com.soprasteria.g4it.backend.external.ecologits.model.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvaluateEcologitsService {

    private static final String WATER_USE = "WATER_USE";
    private static final String MANUFACTURING = "MANUFACTURING";
    private static final String USING = "USING";
    private static final String KO = "KO";
    private static final String OK = "OK";
    private static final String DEFAULT_LOCATION = "WOR";
    private static final Set<String> SUPPORTED_LIFECYCLE_STEPS = Set.of(MANUFACTURING, USING);
    private static final Map<String, Function<EcoImpactPhaseRest, EcoMetricRest>> PHASE_METRICS = Map.of(
            "CLIMATE_CHANGE", EcoImpactPhaseRest::getGwp,
            "RESOURCE_USE", EcoImpactPhaseRest::getAdpe,
            "RESOURCE_USE_FOSSILS", EcoImpactPhaseRest::getPe,
            "WATER_USE", EcoImpactPhaseRest::getWcf
    );
    private static final Map<String, Function<EcoImpactsRest, EcoMetricRest>> TOTAL_METRICS = Map.of(
            "CLIMATE_CHANGE", EcoImpactsRest::getGwp,
            "RESOURCE_USE", EcoImpactsRest::getAdpe,
            "RESOURCE_USE_FOSSILS", EcoImpactsRest::getPe,
            "WATER_USE", EcoImpactsRest::getWcf
    );

    private final EcologitsService ecologitsService;

    public List<ImpactBO> evaluate(final InAiService aiService,
                                   final List<String> activeCriteriaCodes,
                                   final List<String> lifecycleSteps,
                                   final Map<String, String> countryNameToCodeMap) {
        // EcoLogits only provides usage and embodied (manufacturing) impacts for AI services.
        // Transportation and end-of-life lifecycle steps are not evaluated and no rows are produced for them.
        final List<String> supportedLifecycleSteps = lifecycleSteps.stream()
                .filter(SUPPORTED_LIFECYCLE_STEPS::contains)
                .toList();

        if (supportedLifecycleSteps.isEmpty()) {
            return List.of();
        }

        final String resolvedLocation = resolveLocation(aiService.getLocation(), countryNameToCodeMap);
        final int outputTokens;
        try {
            outputTokens = Math.toIntExact(aiService.getOutputTokens());
        } catch (ArithmeticException e) {
            return buildKoRows(activeCriteriaCodes, supportedLifecycleSteps, "output token count exceeds EcoLogits integer limit");
        }

        final EcoEstimationResponseRest response;
        try {
            response = ecologitsService.runEstimation(
                    aiService.getProvider(),
                    aiService.getModel(),
                    outputTokens,
                    resolvedLocation
            );
        } catch (ExternalApiException e) {
            log.warn("EcoLogits estimation failed for AI service '{}' ({}/{})", aiService.getServiceName(), aiService.getProvider(), aiService.getModel(), e);
            return buildKoRows(activeCriteriaCodes, supportedLifecycleSteps, e.getMessage());
        }

        if (response == null || response.getImpacts() == null) {
            return buildKoRows(activeCriteriaCodes, supportedLifecycleSteps, "EcoLogits returned an empty impacts payload");
        }

        if (response.getImpacts().getErrors() != null && !response.getImpacts().getErrors().isEmpty()) {
            final String errorMessage = response.getImpacts().getErrors().stream()
                    .map(message -> String.format("%s: %s", message.getCode(), message.getMessage()))
                    .reduce((left, right) -> left + "; " + right)
                    .orElse("EcoLogits returned calculation errors");
            log.warn("EcoLogits returned business errors for AI service '{}' ({}/{}): {}",
                    aiService.getServiceName(), aiService.getProvider(), aiService.getModel(), errorMessage);
            return buildKoRows(activeCriteriaCodes, supportedLifecycleSteps, errorMessage);
        }

        return buildSuccessRows(activeCriteriaCodes, supportedLifecycleSteps, response.getImpacts());
    }

    private List<ImpactBO> buildSuccessRows(final List<String> activeCriteriaCodes,
                                            final List<String> lifecycleSteps,
                                            final EcoImpactsRest impacts) {
        final List<ImpactBO> results = new ArrayList<>();

        for (String criterion : activeCriteriaCodes) {
            final Function<EcoImpactsRest, EcoMetricRest> totalMetricExtractor = TOTAL_METRICS.get(criterion);
            if (totalMetricExtractor == null) {
                results.addAll(buildKoRows(List.of(criterion), lifecycleSteps,
                        "EcoLogits does not provide this impact criterion for AI services"));
                continue;
            }

            final EcoMetricRest totalMetric = totalMetricExtractor.apply(impacts);
            final String unit = totalMetric == null ? null : totalMetric.getUnit();

            for (String lifecycleStep : lifecycleSteps) {
                results.add(buildSuccessImpact(criterion, lifecycleStep, impacts, unit));
            }
        }

        return results;
    }

    private ImpactBO buildSuccessImpact(final String criterion,
                                        final String lifecycleStep,
                                        final EcoImpactsRest impacts,
                                        final String unit) {
        if (USING.equals(lifecycleStep)) {
            return ImpactBO.builder()
                    .criterion(criterion)
                    .lifecycleStep(lifecycleStep)
                    .unit(unit)
                    .unitImpact(toMeanValue(extractMetric(impacts.getUsage(), criterion)))
                    .indicatorStatus(OK)
                    .build();
        }

        // Only MANUFACTURING remains here since lifecycle steps are filtered to USING/MANUFACTURING upfront.
        final EcoMetricRest embodiedMetric = extractMetric(impacts.getEmbodied(), criterion);
        if (embodiedMetric != null) {
            return ImpactBO.builder()
                    .criterion(criterion)
                    .lifecycleStep(lifecycleStep)
                    .unit(unit)
                    .unitImpact(toMeanValue(embodiedMetric))
                    .indicatorStatus(OK)
                    .build();
        }

        if (WATER_USE.equals(criterion)) {
            return ImpactBO.builder()
                    .criterion(criterion)
                    .lifecycleStep(lifecycleStep)
                    .unit(unit)
                    .unitImpact(0d)
                    .indicatorStatus(OK)
                    .build();
        }

        return ImpactBO.builder()
                .criterion(criterion)
                .lifecycleStep(lifecycleStep)
                .unit(unit)
                .unitImpact(null)
                .indicatorStatus(KO)
                .trace("EcoLogits does not provide this lifecycle step for AI services")
                .build();
    }

    private EcoMetricRest extractMetric(final EcoImpactPhaseRest phase, final String criterion) {
        if (phase == null) {
            return null;
        }
        final Function<EcoImpactPhaseRest, EcoMetricRest> extractor = PHASE_METRICS.get(criterion);
        return extractor == null ? null : extractor.apply(phase);
    }

    /**
     * Resolve the AI service location into the ISO 3166-1 alpha-3 zone code expected by the
     * EcoLogits API. AI services location is captured as a human-readable country name
     * (e.g. "France") at import time, whereas EcoLogits expects a zone code (e.g. "FRA").
     * Falls back to the default "WOR" (world) zone when no location is provided, and passes
     * the raw value through (letting EcoLogits report the error) when it cannot be resolved
     * to a known country - this covers the case where a code is already provided directly.
     *
     * @param rawLocation         the location as captured on the AI service (country name, code, or blank)
     * @param countryNameToCodeMap case-insensitive map of country name to ISO alpha-3 code
     * @return the resolved zone code to send to EcoLogits
     */
    private String resolveLocation(final String rawLocation, final Map<String, String> countryNameToCodeMap) {
        if (StringUtils.isBlank(rawLocation)) {
            return DEFAULT_LOCATION;
        }
        if (countryNameToCodeMap != null) {
            final String code = countryNameToCodeMap.get(rawLocation.toLowerCase(Locale.ROOT));
            if (code != null) {
                return code;
            }
        }
        return rawLocation;
    }

    private Double toMeanValue(final EcoMetricRest metric) {
        if (metric == null || metric.getValue() == null) {
            return null;
        }
        return metric.getValue().getMean();
    }

    private List<ImpactBO> buildKoRows(final List<String> criteria,
                                       final List<String> lifecycleSteps,
                                       final String message) {
        final List<ImpactBO> errors = new ArrayList<>();
        for (String criterion : criteria) {
            for (String lifecycleStep : lifecycleSteps) {
                errors.add(ImpactBO.builder()
                        .criterion(criterion)
                        .lifecycleStep(lifecycleStep)
                        .unitImpact(null)
                        .unit(null)
                        .indicatorStatus(KO)
                        .trace(message)
                        .build());
            }
        }
        return errors;
    }
}