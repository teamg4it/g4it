/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.OutApplicationService;
import com.soprasteria.g4it.backend.apiinout.business.OutDigitalServiceComparisonService;
import com.soprasteria.g4it.backend.apiinout.business.OutPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutVirtualEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceVersionOutputsApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionComparison;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutApplicationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutPhysicalEquipmentRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OutVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Digital Service Input Virtual Equipment Service.
 */
@Slf4j
@Service
@AllArgsConstructor
public class OutDigitalServiceVersionController implements DigitalServiceVersionOutputsApiDelegate {

    /**
     * Service to access physical equipment output data.
     */
    private OutPhysicalEquipmentService outPhysicalEquipmentService;

    /**
     * Service to access virtual equipment output data.
     */
    private OutVirtualEquipmentService outVirtualEquipmentService;

    /**
     * Service to access application output data.
     */
    private OutApplicationService outApplicationService;

    private OutDigitalServiceComparisonService comparisonService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OutPhysicalEquipmentRest>> getDigitalServiceVersionOutputsPhysicalEquipmentsRest(String organization,
                                                                                                                Long workspace,
                                                                                                                String digitalServiceVersionUid) {
        return ResponseEntity.ok().body(outPhysicalEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OutVirtualEquipmentRest>> getDigitalServiceVersionOutputsVirtualEquipmentsRest(String organization,
                                                                                                              Long workspace,
                                                                                                              String digitalServiceVersionUid) {
        return ResponseEntity.ok().body(outVirtualEquipmentService.getByDigitalServiceVersionUid(digitalServiceVersionUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OutApplicationRest>> getDigitalServiceVersionOutputsApplicationsRest(String organization,
                                                                                                    Long workspace,
                                                                                                    String digitalServiceVersionUid) {
        return ResponseEntity.ok().body(outApplicationService.getByDigitalServiceVersionUid(digitalServiceVersionUid));
    }

    @Override
    public ResponseEntity<List<DigitalServiceVersionComparison>> getDigitalServiceVersionComparison(String organization,
                                                                                                    Long workspace,
                                                                                                    String digitalServiceVersionUid1,
                                                                                                    String digitalServiceVersionUid2) {

        List<DigitalServiceVersionComparison> comparisonList =
                comparisonService.compareDigitalServiceVersions(digitalServiceVersionUid1, digitalServiceVersionUid2);

        return ResponseEntity.ok(comparisonList);
    }
}
