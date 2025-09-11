/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules.RulePhysicalEquipmentService;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;

public abstract class AbstractDigitalServiceRule implements DigitalServiceRule {

    @Autowired
    RulePhysicalEquipmentService rulePhysicalEqpService;

    @Override
    public List<LineError> validate(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line) {
        final List<LineError> errors = new ArrayList<>();
        validateCommonRules(locale, physicalEquipment, filename, line, errors);
        validateSpecificRules(locale, physicalEquipment, filename, line, errors);
        return errors;
    }

    protected void validateCommonRules(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {
        // Add common validation logic here
        rulePhysicalEqpService.checkElectricityConsumption(locale, filename, line, physicalEquipment.getElectricityConsumption())
                .ifPresent(errors::add);
        rulePhysicalEqpService.checkDurationHour(locale, filename, line, physicalEquipment.getDurationHour())
                .ifPresent(errors::add);
    }

    protected abstract void validateSpecificRules(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors);
}