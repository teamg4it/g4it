/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.engine.boaviztapi;

import com.soprasteria.g4it.backend.apievaluating.model.ExternalTraceBO;
import com.soprasteria.g4it.backend.apievaluating.model.ImpactBO;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.JsonUtils;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaImpactRest;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvaluateBoaviztapiService {

    @Autowired
    private BoaviztapiService boaviztapiService;

    /**
     * Evaluate a virtual equipment with boaviztapi
     *
     * @param inVirtualEquipment the virtual equipment
     * @param criteria           the criterion list
     * @param lifecycleSteps     the lifecycle steps
     * @return the list of impact
     */
    public List<ImpactBO> evaluate(final InVirtualEquipment inVirtualEquipment,
                                   final List<String> criteria, final List<String> lifecycleSteps
    ) {
        final List<ImpactBO> result = new ArrayList<>();
        BoaResponseRest response;

        try {
            response = boaviztapiService.runBoaviztCalculations(inVirtualEquipment);
        } catch (ExternalApiException e) {
            return getErrors(criteria, lifecycleSteps, e.getStatusCode(), e.getMessage());
        }

        var criteriaImpactMap = response == null ?
                new HashMap<String, BoaImpactRest>() :
                Map.ofEntries(
                        new AbstractMap.SimpleEntry<>("CLIMATE_CHANGE", response.getImpacts().getGwp()),
                        new AbstractMap.SimpleEntry<>("RESOURCE_USE", response.getImpacts().getAdpe()),
                        new AbstractMap.SimpleEntry<>("IONISING_RADIATION", response.getImpacts().getIr()),
                        new AbstractMap.SimpleEntry<>("ACIDIFICATION", response.getImpacts().getAp()),
                        new AbstractMap.SimpleEntry<>("PARTICULATE_MATTER", response.getImpacts().getPm()),
                        new AbstractMap.SimpleEntry<>("OZONE_DEPLETION", response.getImpacts().getOdp()),
                        new AbstractMap.SimpleEntry<>("PHOTOCHEMICAL_OZONE_FORMATION", response.getImpacts().getPocp()),
                        new AbstractMap.SimpleEntry<>("EUTROPHICATION_TERRESTRIAL", response.getImpacts().getEpt()),
//                        new AbstractMap.SimpleEntry<>("EUTROPHICATION_FRESHWATER", response.getImpacts().getEpf()),
                        new AbstractMap.SimpleEntry<>("EUTROPHICATION_MARINE", response.getImpacts().getEpm()),
                        new AbstractMap.SimpleEntry<>("RESOURCE_USE_FOSSILS", response.getImpacts().getAdpf())
                );

        for (String criterion : criteria) {
            BoaImpactRest impact = null;
            if (criteriaImpactMap.containsKey(criterion)) {
                impact = criteriaImpactMap.get(criterion);
            }

            for (String lifecycleStep : lifecycleSteps) {

                Double unitImpact = getUnitImpact(impact, lifecycleStep);
                String unit = unitImpact == null ? null : impact.getUnit();
                String indicatorStatus = unitImpact == null ? "ERROR" : "OK";

                result.add(ImpactBO.builder()
                        .criterion(criterion)
                        .lifecycleStep(lifecycleStep)
                        .unitImpact(unitImpact)
                        .unit(unit)
                        .indicatorStatus(indicatorStatus)
                        .build());
            }
        }

        return result;
    }

    /**
     * Get unit impact from boavizta impact
     *
     * @param impact        the boavizta impact
     * @param lifecycleStep the lifecycle step
     * @return the unit impact
     */
    private Double getUnitImpact(final BoaImpactRest impact, final String lifecycleStep) {
        if (impact == null) return null;
        return switch (lifecycleStep) {
            case Constants.MANUFACTURING -> impact.getEmbedded().getValue();
            case Constants.USING -> impact.getUse().getValue();
            default -> null;
        };
    }

    /**
     * Get Error list from exception
     *
     * @param criteria       the list of criterion
     * @param lifecycleSteps the list of lifecycleStep
     * @param statusCode     the statusCode
     * @param message        the error message
     * @return list of impact in error
     */
    private List<ImpactBO> getErrors(final List<String> criteria, final List<String> lifecycleSteps, final int statusCode, final String message) {
        List<ImpactBO> errors = new ArrayList<>();
        String externalTraceBOJson = JsonUtils.toJson(ExternalTraceBO.builder()
                .externalApi("boaviztapi/v1/cloud/instance")
                .code(statusCode)
                .error(message)
                .build());

        for (String criterion : criteria) {
            for (String lifecycleStep : lifecycleSteps) {
                errors.add(ImpactBO.builder()
                        .criterion(criterion)
                        .lifecycleStep(lifecycleStep)
                        .unitImpact(null)
                        .unit(null)
                        .indicatorStatus("ERROR")
                        .trace(externalTraceBOJson)
                        .build());
            }
        }

        return errors;
    }

}
