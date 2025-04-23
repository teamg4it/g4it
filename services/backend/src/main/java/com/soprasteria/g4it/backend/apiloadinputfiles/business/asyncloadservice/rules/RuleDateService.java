/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

@Service
public class RuleDateService {

    @Autowired
    MessageSource messageSource;

    public Optional<LineError> checkDatesPurcaseRetrieval(Locale locale,String filename, int line, LocalDate datePurchase, LocalDate dateRetrieval) {

        if (datePurchase == null) return Optional.empty();

        if (datePurchase.isEqual(Constants.ERROR_DATE_FORMAT)) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage(
                    "date.format.purchase.incorrect",
                    new String[]{},
                    locale)));
        }

        if (dateRetrieval == null) return Optional.empty();
        if (dateRetrieval.isEqual(Constants.ERROR_DATE_FORMAT)) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage(
                    "date.format.retrieval.incorrect",
                    new String[]{},
                    locale)));
        }

        if (datePurchase.isAfter(dateRetrieval)) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage(
                    "date.purchase.retrieval.incorrect",
                    new String[]{},
                    locale)));
        }

        return Optional.empty();
    }
}
