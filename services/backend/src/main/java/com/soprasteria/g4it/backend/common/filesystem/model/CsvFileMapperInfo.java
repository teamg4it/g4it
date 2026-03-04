/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.model;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Setter
@ConfigurationProperties(prefix = "filesystem.config.headers")
public class CsvFileMapperInfo implements FileMapperInfo {

    private List<Header> application = List.of(
            Header.builder().name("nomApplication").optional(false).build()
    );

    private List<Header> datacenter = List.of(
            Header.builder().name("nomCourtDatacenter").optional(false).build()
    );

    private List<Header> equipementPhysique = List.of(
            Header.builder().name("nomEquipementPhysique").optional(false).build()
    );

    private List<Header> equipementVirtuel = List.of(
            Header.builder().name("nomEquipementVirtuel").optional(false).build()
    );

    private List<Header> virtualEquipment;

    private List<Header> inventory;
    private List<Header> physicalEquipmentIndicator;
    private List<Header> virtualEquipmentIndicator;
    private List<Header> applicationIndicator;

    private List<Header> physicalEquipmentIndicatorDigitalService;

    private List<Header> virtualEquipmentIndicatorDigitalService;

    private List<Header> outAiReco;
    private List<Header> aiParameters;
    private List<Header> aiInfrastructure;

    @Override
    public List<Header> getMapping(final FileType type) {
        return switch (type) {
            case UNKNOWN -> Collections.emptyList();
            case DATACENTER -> new ArrayList<>(safe(datacenter));
            case EQUIPEMENT_VIRTUEL -> new ArrayList<>(safe(equipementVirtuel));
            case VIRTUAL_EQUIPMENT -> new ArrayList<>(safe(virtualEquipment));
            case EQUIPEMENT_PHYSIQUE -> new ArrayList<>(safe(equipementPhysique));
            case APPLICATION -> new ArrayList<>(safe(application));
            case PHYSICAL_EQUIPMENT_INDICATOR -> new ArrayList<>(safe(physicalEquipmentIndicator));
            case VIRTUAL_EQUIPMENT_INDICATOR -> new ArrayList<>(safe(virtualEquipmentIndicator));
            case APPLICATION_INDICATOR -> new ArrayList<>(safe(applicationIndicator));
            case INVENTORY -> new ArrayList<>(safe(inventory));
            case PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE ->
                    new ArrayList<>(safe(physicalEquipmentIndicatorDigitalService));
            case VIRTUAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE ->
                    new ArrayList<>(safe(virtualEquipmentIndicatorDigitalService));
            case OUT_AI_RECO -> new ArrayList<>(safe(outAiReco));
            case IN_AI_PARAMETERS -> new ArrayList<>(safe(aiParameters));
            case IN_AI_INFRASTRUCTURE -> new ArrayList<>(safe(aiInfrastructure));
        };
    }


    public Set<String> getHeaderFields(final FileType fileType, final boolean mandatory) {
        return getMapping(fileType).stream()
                .filter(h -> !mandatory || !h.isOptional())
                .map(Header::getName)
                .collect(Collectors.toSet());
    }

    private List<Header> safe(List<Header> list) {
        return list == null ? Collections.emptyList() : list;
    }

}
