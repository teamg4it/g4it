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
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class EvaluateBoaviztapiService {


    private final BoaviztapiService boaviztapiService;

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

        // Use a null-tolerant map: BoaviztAPI may not return every criterion
        // (e.g. missing field, or "not implemented" mapped to null), and
        // Map.ofEntries/Map.of do not accept null values.
        var criteriaImpactMap = new HashMap<String, BoaImpactRest>();
        if (response != null && response.getImpacts() != null) {
            criteriaImpactMap.put("CLIMATE_CHANGE", response.getImpacts().getGwp());
            criteriaImpactMap.put("RESOURCE_USE", response.getImpacts().getAdpe());
            criteriaImpactMap.put("IONISING_RADIATION", response.getImpacts().getIr());
            criteriaImpactMap.put("ACIDIFICATION", response.getImpacts().getAp());
            criteriaImpactMap.put("PARTICULATE_MATTER", response.getImpacts().getPm());
            criteriaImpactMap.put("OZONE_DEPLETION", response.getImpacts().getOdp());
            criteriaImpactMap.put("PHOTOCHEMICAL_OZONE_FORMATION", response.getImpacts().getPocp());
            criteriaImpactMap.put("EUTROPHICATION_TERRESTRIAL", response.getImpacts().getEpt());
            criteriaImpactMap.put("EUTROPHICATION_FRESHWATER", response.getImpacts().getEpf());
            criteriaImpactMap.put("EUTROPHICATION_MARINE", response.getImpacts().getEpm());
            criteriaImpactMap.put("RESOURCE_USE_FOSSILS", response.getImpacts().getAdpf());
            criteriaImpactMap.put("WATER_USE", response.getImpacts().getWu());
        }

        for (String criterion : criteria) {
            BoaImpactRest impact = null;
            if (criteriaImpactMap.containsKey(criterion)) {
                impact = criteriaImpactMap.get(criterion);
            }

            for (String lifecycleStep : lifecycleSteps) {

                Double unitImpact = getUnitImpact(impact, lifecycleStep);
                String unit = unitImpact == null ? null : impact.getUnit();
                String indicatorStatus = unitImpact == null ? "ERROR" : "OK";
                String traceMessage = null;
                if (lifecycleStep.equalsIgnoreCase(Constants.TRANSPORTATION)) {
                    // BoaviztAPI returns the impact per year, we need to convert it to per hour
                    indicatorStatus = "OK";
                    traceMessage = Constants.BOAVIZTA_TRACE_TRANSPORTATION;
                }

                result.add(ImpactBO.builder()
                        .criterion(criterion)
                        .lifecycleStep(lifecycleStep)
                        .unitImpact(unitImpact)
                        .unit(unit)
                        .indicatorStatus(indicatorStatus).trace(traceMessage)
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
            // embedded/use can be null when BoaviztAPI returns "not implemented"
            // instead of a value object for this criterion/phase
            case Constants.MANUFACTURING -> impact.getEmbedded() == null ? null : impact.getEmbedded().getValue();
            case Constants.USING -> impact.getUse() == null ? null : impact.getUse().getValue();
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
