/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceType;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.GenericRuleService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RuleDateService;
import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RulePhysicalEquipmentService;
import com.soprasteria.g4it.backend.common.model.Context;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CheckPhysicalEquipmentService {

    @Autowired
    GenericRuleService genericRuleService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    RuleDateService ruleDateService;
    @Autowired
    RulePhysicalEquipmentService rulePhysicalEqpService;

    @Autowired
    private DigitalServiceRuleFactory digitalServiceRuleFactory;

    /**
     * Check a physical equipment object
     *
     * @param context           the context
     * @param physicalEquipment the physical equipment
     * @param line              the line number
     * @return the list of errors
     */
    public List<LineError> checkRules(final Context context, final InPhysicalEquipmentRest physicalEquipment, final String filename, final int line) {
        List<LineError> errors = new ArrayList<>();
        final boolean isDigitalService = context.getDigitalServiceUid() != null;

        // check InPhysicalEquipmentRest constraint violations
        genericRuleService.checkViolations(physicalEquipment, filename, line).ifPresent(errors::add);

        // check location is in country referential (itemImpacts - category = 'electricity-mix')
        genericRuleService.checkLocation(context.getLocale(), context.getSubscriber(), filename, line, physicalEquipment.getLocation())
                .ifPresent(errors::add);

        // check type is in itemTypes referential
        genericRuleService.checkType(context.getLocale(), context.getSubscriber(), filename, line, physicalEquipment.getType(), isDigitalService)
                .ifPresent(errors::add);


        // check date purchase < date retrieval
        if (!(isDigitalService && DigitalServiceType.NETWORK.getValue().equals(physicalEquipment.getType()))) {
            ruleDateService.checkDatesPurchaseRetrieval(context.getLocale(), filename, line, physicalEquipment.getDatePurchase(), physicalEquipment.getDateWithdrawal(), isDigitalService)
                    .ifPresent(errors::add);
        }

        // check model for digital service
        final String type = physicalEquipment.getType();
        if (isDigitalService && Objects.nonNull(type)) {
            DigitalServiceRule rule = digitalServiceRuleFactory.getRule(type);
            if (rule != null) {
                errors.addAll(rule.validate(context.getLocale(), physicalEquipment, filename, line));
            } else {
                errors.add(new LineError(filename, line,
                        messageSource.getMessage("physical.eqp.type.invalid",
                                new String[]{type},
                                context.getLocale())));
            }
        }

        return errors;
    }
}
