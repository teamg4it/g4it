/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.business.asyncloadservice.rules;

import com.soprasteria.g4it.backend.common.model.LineError;
import com.soprasteria.g4it.backend.common.utils.InfrastructureType;
import com.soprasteria.g4it.backend.external.boavizta.business.BoaviztapiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class RuleVirtualEquipmentService {

    @Autowired
    MessageSource messageSource;
    @Autowired
    BoaviztapiService boaviztapiService;


    /**
     * @param locale             the Locale
     * @param line               current line number
     * @param infrastructureType the infrastructureType
     * @return errors
     */
    public Optional<LineError> checkInfrastructureType(Locale locale, String filename, int line, String infrastructureType) {
        if (infrastructureType != null &&
                !InfrastructureType.CLOUD_SERVICES.name().equals(infrastructureType) &&
                !InfrastructureType.NON_CLOUD_SERVERS.name().equals(infrastructureType)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("typeInfrastructure.values", new String[]{infrastructureType}, locale)
            ));
        }
        return Optional.empty();
    }

    public Optional<LineError> checkPhysicalEquipmentLinked(Locale locale, String filename, int line, final String infrastructureType, final String physicalEquipmentName) {

        if (!InfrastructureType.CLOUD_SERVICES.name().equals(infrastructureType) && physicalEquipmentName == null) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage(
                    "virtual.equipment.must.have.physical.equipment",
                    new String[]{},
                    locale)));
        }

        return Optional.empty();
    }

    public Optional<LineError> checkType(Locale locale, String filename, int line, String typeEqv) {
        if (StringUtils.isEmpty(typeEqv)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("virtual.equipment.must.have.typeEqv",
                            new String[]{},
                            locale)));
        }
        return Optional.empty();
    }

    /**
     * @param locale   the Locale
     * @param line     current line number
     * @param location the location
     * @return error
     */
    public Optional<LineError> checkCloudLocation(Locale locale, String filename, int line, String location) {
        if (StringUtils.isEmpty(location)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.location.blank", new String[]{}, locale)
            ));
        }
        // check location is in BoaviztAPI referential
        if (!boaviztapiService.getCountryMap().containsKey(location)) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage("boaviztAPI.referential.location.not.exist", new String[]{location}, locale)));
        }
        return Optional.empty();
    }

    /**
     * @param locale   the Locale
     * @param line     current line number
     * @param provider the provider
     * @return error
     */
    public Optional<LineError> checkCloudProvider(Locale locale, String filename, int line, String provider) {
        if (StringUtils.isEmpty(provider)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.provider.blank", new String[]{}, locale)
            ));
        }
        if (!boaviztapiService.getProviderList().contains(provider)) {
            return Optional.of(new LineError(filename, line, messageSource.getMessage("boaviztAPI.referential.provider.not.exist",
                    new String[]{provider}, locale)));
        }
        return Optional.empty();
    }

    /**
     * @param locale       the Locale
     * @param line         current line number
     * @param provider     the provider
     * @param instanceType the instanceType
     * @return error
     */
    public Optional<LineError> checkCloudInstanceType(Locale locale, String filename, int line, String provider, String instanceType) {
        // Check if instanceType is empty
        if (StringUtils.isEmpty(instanceType)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.typeInstance.blank", new String[]{}, locale)
            ));
        }
        // Check if provider is not null and instanceType exists
        if (!StringUtils.isEmpty(provider) && boaviztapiService.getProviderList().contains(provider)) {
            List<String> instanceList = boaviztapiService.getInstanceList(provider);
            if (!instanceList.contains(instanceType)) {
                return Optional.of(new LineError(filename, line,
                        messageSource.getMessage("boaviztAPI.referential.typeInstance.not.exist",
                                new String[]{instanceType, provider}, locale)));
            }
        }
        return Optional.empty();
    }

    /**
     * @param locale   the Locale
     * @param line     current line number
     * @param quantity the quantity
     * @return errors
     */
    public Optional<LineError> checkCloudQuantity(Locale locale, String filename, int line, Double quantity) {
        if (quantity == null || quantity <= 0) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.quantity.blank", new String[]{}, locale)
            ));
        }
        return Optional.empty();
    }

    /**
     * @param locale        the Locale
     * @param line          current line number
     * @param usageDuration the workload
     * @return errors
     */
    public Optional<LineError> checkCloudUsageDuration(Locale locale, String filename, int line, Double usageDuration) {
        if (usageDuration == null || usageDuration < 0) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.duration.blank", new String[]{}, locale)
            ));
        } else if (usageDuration > 8760) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.duration.invalid", new String[]{}, locale)
            ));
        }
        return Optional.empty();
    }

    /**
     * @param locale        the Locale
     * @param line          current line number
     * @param usageDuration the usageDuration
     * @return errors
     */
    public Optional<LineError> checkCloudWorkload(Locale locale, String filename, int line, Double usageDuration) {
        if (usageDuration == null) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.workload.blank", new String[]{}, locale)
            ));
        } else if (usageDuration < 0 || usageDuration > 100) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.workload.invalid", new String[]{}, locale)
            ));
        }
        return Optional.empty();
    }

    /**
     * @param locale               the Locale
     * @param line                 current line number
     * @param virtualEquipmentName virtual equipment name
     * @return errors
     */
    public Optional<LineError> checkVirtualEquipmentName(Locale locale, String filename, int line, String virtualEquipmentName, Set<String> virtualEquipmentNames,
                                                         boolean isCloudService) {
        if (virtualEquipmentName == null) {
            String messageCode = isCloudService ? "cloud.equipment.blank" : "nomequipementvirtuel.not.blank";
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage(messageCode, new String[]{}, locale)
            ));
        } else if (virtualEquipmentNames.contains(virtualEquipmentName)) {
            return Optional.of(new LineError(filename, line,
                    messageSource.getMessage("cloud.equipment.unique", new String[]{}, locale)
            ));
        }
        virtualEquipmentNames.add(virtualEquipmentName);
        return Optional.empty();
    }

}
