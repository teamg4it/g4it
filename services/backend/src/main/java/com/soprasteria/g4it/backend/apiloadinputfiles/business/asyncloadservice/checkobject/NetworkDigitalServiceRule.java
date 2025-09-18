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
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.NetworkTypeRefRepository;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class NetworkDigitalServiceRule extends AbstractDigitalServiceRule {

    @Autowired
    MessageSource messageSource;
    @Autowired
    DigitalServiceReferentialService digitalServiceRefService;
    @Autowired
    NetworkTypeRefRepository networkTypeRefRepository;

    @Override
    protected void validateSpecificRules(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {
        // Add specific validation logic for Network Digital Service here
        checkDigitalServiceModel(locale, filename, line, physicalEquipment.getModel()).ifPresent(errors::add);

        checkType(locale, physicalEquipment, filename, line, errors).ifPresent(errors::add);

        checkAndSaveDates(locale, filename, line,
                physicalEquipment);
    }

    private Optional<LineError> checkType(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {
        String type = physicalEquipment.getType();
        if (!DigitalServiceType.NETWORK.getValue().equals(type)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("physical.eqp.type.invalid",
                            new String[]{type},
                            locale)));
        }
        return Optional.empty();
    }

    private void checkAndSaveDates(Locale locale, String filename, int line, InPhysicalEquipmentRest physicalEquipment) {

        LocalDate defaultDatePurchase = LocalDate.parse(Constants.NETWORK_DATE_PURCHASE);
        LocalDate defaultDateWithdrawal = LocalDate.parse(Constants.NETWORK_DATE_WITHDRAWAL);

        //set datePurchase  to '2020-01-01'
        physicalEquipment.setDatePurchase(defaultDatePurchase);

        //set dateWithdrawal to '2021-01-01'
        physicalEquipment.setDateWithdrawal(defaultDateWithdrawal);

    }

    private Optional<LineError> checkDigitalServiceModel(Locale locale, String filename, int line, String model) {
        //Consistent with the list of model available in ref_network_type.reference
        Optional<NetworkTypeRef> refNetworkType = networkTypeRefRepository.findByReference(model);

        if (refNetworkType.isEmpty()) {
            return Optional.of(new LineError(
                    filename,
                    line,
                    messageSource.getMessage("referential.model.not.exist", new String[]{model}, locale)
            ));
        }

        return Optional.empty();
    }
}
