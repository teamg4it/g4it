/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.OutApplicationService;
import com.soprasteria.g4it.backend.apiinout.business.OutPhysicalEquipmentService;
import com.soprasteria.g4it.backend.apiinout.business.OutVirtualEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceOutputsApiDelegate;
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
public class OutDigitalServiceController implements DigitalServiceOutputsApiDelegate {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OutPhysicalEquipmentRest>> getDigitalServiceOutputsPhysicalEquipmentsRest(String organization,
                                                                                                         Long workspace,
                                                                                                         String digitalServiceUid) {
        return ResponseEntity.ok().body(outPhysicalEquipmentService.getByDigitalServiceUid(digitalServiceUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OutVirtualEquipmentRest>> getDigitalServiceOutputsVirtualEquipmentsRest(String organization,
                                                                                                       Long workspace,
                                                                                                       String digitalServiceUid) {
        return ResponseEntity.ok().body(outVirtualEquipmentService.getByDigitalServiceUid(digitalServiceUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OutApplicationRest>> getDigitalServiceOutputsApplicationsRest(String organization,
                                                                                             Long workspace,
                                                                                             String digitalServiceUid) {
        return ResponseEntity.ok().body(outApplicationService.getByDigitalServiceUid(digitalServiceUid));
    }
}
