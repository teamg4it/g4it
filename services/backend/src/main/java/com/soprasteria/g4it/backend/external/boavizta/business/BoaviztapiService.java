/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.boavizta.business;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.external.boavizta.client.BoaviztapiClient;
import com.soprasteria.g4it.backend.external.boavizta.model.response.BoaResponseRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class BoaviztapiService {

    /**
     * BoaviztapiClient
     */
    private BoaviztapiClient boaviztapiClient;

    public static final String BOAVIZTAPI_VERSION = "1.3";
    public static final String BOAVIZTAPI_ENGINE = "BoaviztAPI";
    private static final double HOURS_PER_YEAR = 24d * 365d;

    /**
     * Get BoaviztAPI countries with code.
     *
     * @return country map.
     */
    @Cacheable("boaviztaGetCountryMap")
    public Map<String, String> getCountryMap() {
        return boaviztapiClient.getAllCountries();
    }

    /**
     * Get BoaviztAPI cloud providers.
     *
     * @return providers name list (string).
     */
    @Cacheable("boaviztaGetProviderList")
    public List<String> getProviderList() {
        return boaviztapiClient.getAllProviders();
    }

    /**
     * Get BoaviztAPI instances for one specific cloud provider.
     *
     * @return instances name list (string).
     */
    @Cacheable("boaviztaGetInstanceList")
    public List<String> getInstanceList(String cloudProvider) {
        return boaviztapiClient.getAllInstances(cloudProvider);
    }

    /**
     * Run boavitAPI calculations
     *
     * @param virtualEquipment the virtual equipment
     * @return the Boa response
     */
    public BoaResponseRest runBoaviztCalculations(InVirtualEquipment virtualEquipment) {
        return boaviztapiClient.runCalculation(virtualEquipment);
    }

    public double computeAnnualElectricityKwhRaw(
            Double avgPowerW,
            double durationHours
    ) {
        if (avgPowerW == null) return 0d;

        return avgPowerW * (durationHours / 1000d);
    }


    public Optional<Double> extractAvgPowerW(BoaResponseRest response) {
        if (response == null ||
                response.getVerbose() == null ||
                response.getVerbose().getAvgPower() == null ||
                response.getVerbose().getAvgPower().getValue() == null) {
            return Optional.empty();
        }
        Double avgPower = response.getVerbose().getAvgPower().getValue();
        return Optional.of(avgPower);
    }

}