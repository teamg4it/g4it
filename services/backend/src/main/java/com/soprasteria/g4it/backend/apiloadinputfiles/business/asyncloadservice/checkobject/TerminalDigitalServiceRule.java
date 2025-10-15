/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceType;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DeviceTypeRefRepository;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TerminalDigitalServiceRule extends AbstractDigitalServiceRule {

    @Autowired
    MessageSource messageSource;
    @Autowired
    DigitalServiceReferentialService digitalServiceRefService;
    @Autowired
    DeviceTypeRefRepository deviceTypeRefRepository;

    @Override
    protected void validateSpecificRules(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {
        // Add specific validation logic for Terminal Network Digital Service here
        checkDigitalServiceModel(locale, filename, line, physicalEquipment.getModel()).ifPresent(errors::add);

        //check Type
        checkType(locale, physicalEquipment, filename, line, errors);

        checkNumberOfUsers(locale, physicalEquipment, filename, line, errors);
        rulePhysicalEqpService.checkDurationHour(locale, filename, line, physicalEquipment.getDurationHour())
                .ifPresent(errors::add);

    }

    private void checkNumberOfUsers(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {

        if (physicalEquipment.getNumberOfUsers() == null || physicalEquipment.getNumberOfUsers() <= 0) {
            errors.add(Optional.of(new LineError(filename, line,
                    messageSource.getMessage("physical.eqp.numberOfUsers.invalid",
                            new String[]{String.valueOf(physicalEquipment.getNumberOfUsers())},
                            locale))).get());
        } else if (physicalEquipment.getDurationHour() != null && physicalEquipment.getDurationHour() > 0) {
            //calculate the quantity (quantity = nbUser * durationHour/8760)
            double quantity = physicalEquipment.getNumberOfUsers() * physicalEquipment.getDurationHour() / 8760;
            physicalEquipment.setQuantity(quantity);
        }

    }

    private void checkType(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {
        String type = physicalEquipment.getType();
        if (!DigitalServiceType.TERMINAL.getValue().equals(type)) {
            errors.add(Optional.of(new LineError(filename, line,
                    messageSource.getMessage("physical.eqp.type.invalid",
                            new String[]{type},
                            locale))).get());
        }
    }

    private Optional<LineError> checkDigitalServiceModel(Locale locale, String filename, int line, String model) {
        //Consistent with the list of model available in ref_device_type.reference
        Optional<DeviceTypeRef> refDeviceType =  deviceTypeRefRepository.findByReference(model);
        if (refDeviceType.isEmpty()) {
            return Optional.of(new LineError(
                    filename,
                    line,
                    messageSource.getMessage("referential.model.not.exist", new String[]{model}, locale)
            ));
        }

        return Optional.empty();
    }
}
