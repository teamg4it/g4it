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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class CommonValidationUtil {

    private DigitalServiceReferentialService digitalServiceReferentialService;
    private BoaviztapiService boaviztapiService;

    public boolean validateCountry(final String country){
        return digitalServiceReferentialService.getCountry().stream().anyMatch(inputCountry -> inputCountry.equals(country));
    }

    public boolean validateboaviztaCountry(final String country) {
        final String trimmed = country.trim();

        final Map<String, String> countryMap = boaviztapiService.getCountryMap();
        if (countryMap == null || countryMap.isEmpty()) {
            return false;
        }

        return countryMap.values().stream()
                .filter(Objects::nonNull)
                .anyMatch(value -> value.equalsIgnoreCase(trimmed));
    }
}
