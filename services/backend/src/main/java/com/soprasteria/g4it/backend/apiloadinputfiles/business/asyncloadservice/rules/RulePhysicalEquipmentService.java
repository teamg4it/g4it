/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.common.model.LineError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class RulePhysicalEquipmentService {

    @Autowired
    MessageSource messageSource;
    @Autowired
    DigitalServiceReferentialService digitalServiceRefService;

    public Optional<LineError> checkElectricityConsumption(Locale locale, String filename, int line, final Double electricityConsumption) {

        if (electricityConsumption == null) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage(
                    "electricity.consumption.blank",
                    new String[]{},
                    locale)));
        }

        return Optional.empty();
    }

    public Optional<LineError> checkDurationHour(Locale locale, String filename, int line, final Double usageDuration) {
        if (usageDuration == null || usageDuration < 0) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("durationHour.blank", new String[]{}, locale)
            ));
        } else if (usageDuration > 8760) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("durationHour.invalid", new String[]{}, locale)
            ));
        }
        return Optional.empty();
    }

}
