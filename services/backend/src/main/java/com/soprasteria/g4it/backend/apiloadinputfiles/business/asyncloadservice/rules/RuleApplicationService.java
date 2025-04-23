/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class RuleApplicationService {

    @Autowired
    MessageSource messageSource;

    public Optional<LineError> checkVirtualEquipmentLinked(Locale locale,final String filename, int line, final String virtualEquipmentName) {

        if (virtualEquipmentName == null) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage(
                    "application.must.have.virtual.equipment",
                    new String[]{},
                    locale)));
        }

        return Optional.empty();
    }
}
