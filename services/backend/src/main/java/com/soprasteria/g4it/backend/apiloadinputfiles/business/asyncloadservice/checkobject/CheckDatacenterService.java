/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckDatacenterService {

    @Autowired
    GenericRuleService genericRuleService;

    /**
     * Check a datacenter object
     *
     * @param context    the context
     * @param datacenter the datacenter
     * @param line       the line number
     * @return the list of errors
     */
    public List<LineError> checkRules(final Context context, final InDatacenterRest datacenter,final String filename, final int line) {
        List<LineError> errors = new ArrayList<>();

        // check InDatacenterRest constraint violations
        genericRuleService.checkViolations(datacenter,filename, line).ifPresent(errors::add);

        // check location is in country referential
        genericRuleService.checkLocation(context.getLocale(), context.getSubscriber(), filename, line, datacenter.getLocation()).ifPresent(errors::add);

        return errors;
    }
}
