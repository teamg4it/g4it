/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.checkobject;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceReferentialService;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerHostBO;
import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class NonCloudDigitalServiceRule extends AbstractDigitalServiceRule {

    @Autowired
    MessageSource messageSource;
    @Autowired
    DigitalServiceReferentialService digitalServiceRefService;

    @Override
    protected void validateSpecificRules(Locale locale, InPhysicalEquipmentRest physicalEquipment, String filename, int line, List<LineError> errors) {
        checkDigitalServiceModel(locale, filename, line, physicalEquipment.getModel(), physicalEquipment).ifPresent(errors::add);
    }

    private Optional<LineError> checkDigitalServiceModel(Locale locale, String filename, int line, String model, InPhysicalEquipmentRest physicalEquipment) {
        List<ServerHostBO> allHosts = digitalServiceRefService.getServerHosts();
        // Check if the model exists in hosts
        ServerHostBO matchingHost = allHosts.stream()
                .filter(host -> host.getReference().equals(model))
                .findFirst()
                .orElse(null);

        if (matchingHost == null) {
            return Optional.of(new LineError(
                    filename,
                    line,
                    messageSource.getMessage("referential.model.not.exist", new String[]{model}, locale)
            ));
        }

        if ("Storage".equals(matchingHost.getType())) {
            if (physicalEquipment.getSizeDiskGb() == null) {
                return Optional.of(new LineError(
                        filename,
                        line,
                        messageSource.getMessage("disk.size.blank", new String[]{}, locale)
                ));
            }
        }
        if ("Compute".equals(matchingHost.getType())) {
            if (physicalEquipment.getCpuCoreNumber() == null) {
                return Optional.of(new LineError(
                        filename,
                        line,
                        messageSource.getMessage("cpu.number.blank", new String[]{}, locale)
                ));
            }
        }
        return Optional.empty();

    }
}
