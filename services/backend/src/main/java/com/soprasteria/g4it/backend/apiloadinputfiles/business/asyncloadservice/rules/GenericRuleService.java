/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerHostBO;
import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.ValidationUtils;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class GenericRuleService {

    @Autowired
    MessageSource messageSource;

    @Autowired
    Validator validator;

    @Autowired
    ReferentialGetService referentialGetService;


    /**
     * Check location is in referential item impacts
     *
     * @param subscriber the subscriber
     * @param location   the location
     * @return error
     */
    public Optional<LineError> checkLocation(Locale locale, String subscriber, String filename, int line, String location) {

        if (referentialGetService.getCountries(subscriber).contains(location)) return Optional.empty();
        if (referentialGetService.getCountries(null).contains(location)) return Optional.empty();

        return Optional.of(new LineError(filename,line, messageSource.getMessage("referential.location.not.exist", new String[]{location}, locale)));
    }

    /**
     * Check type is in referential item types
     *
     * @param subscriber the subscriber
     * @param type       the location
     * @return error
     */
   public Optional<LineError> checkType(Locale locale, String subscriber, String filename, int line, String type, boolean isDigitalService) {

        if (StringUtils.isEmpty(type)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("physical.equipment.must.have.type",
                            new String[]{},
                            locale)));
        }
        if (isDigitalService) {
            if( !"Shared Server".equals(type) && !"Dedicated Server".equals(type))
                return Optional.of(new LineError(filename, line,
                        messageSource.getMessage("physical.eqp.type.invalid",
                                new String[]{type},
                                locale)));
        }
        else {
            if (referentialGetService.getItemTypes(type, subscriber).isEmpty() &&
                    referentialGetService.getItemTypes(type, null).isEmpty()) {

                return Optional.of(new LineError(
                        filename,
                        line,
                        messageSource.getMessage("referential.type.not.exist", new String[]{type}, locale)
                ));
            }

        } return Optional.empty();

    }

    /**
     * Check violations for object
     *
     * @param object the object
     * @param line   the line number
     * @return the LineError if violation
     */
    public Optional<LineError> checkViolations(final Object object, String filename, final int line) {
        return ValidationUtils.getViolations(validator.validate(object)).map(s -> new LineError(filename,line, s));
    }
}
