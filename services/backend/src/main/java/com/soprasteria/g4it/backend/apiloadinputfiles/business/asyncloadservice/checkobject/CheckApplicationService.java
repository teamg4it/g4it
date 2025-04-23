/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RuleApplicationService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckApplicationService {

    @Autowired
    GenericRuleService genericRuleService;

    @Autowired
    RuleApplicationService ruleApplicationService;

    /**
     * Check a virtual equipment object
     *
     * @param context     the context
     * @param application the application
     * @param line        the line number
     * @return the list of errors
     */
    public List<LineError> checkRules(final Context context, final InApplicationRest application, final String filename, final int line) {
        List<LineError> errors = new ArrayList<>();

        // check InApplicationRest constraint violations
        genericRuleService.checkViolations(application, filename, line).ifPresent(errors::add);

        // check application has a virtual equipment linked
        ruleApplicationService.checkVirtualEquipmentLinked(context.getLocale(),filename, line, application.getVirtualEquipmentName())
                .ifPresent(errors::add);

        return errors;
    }
}
