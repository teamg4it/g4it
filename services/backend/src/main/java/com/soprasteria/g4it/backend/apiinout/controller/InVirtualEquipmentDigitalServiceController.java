/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceInputsVirtualEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Digital Service Input Virtual Equipment Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InVirtualEquipmentDigitalServiceController implements DigitalServiceInputsVirtualEquipmentApiDelegate {

    /**
     * Service to access virtual equipment input data.
     */
    private InVirtualEquipmentService inVirtualEquipmentService;
    private DigitalServiceService digitalServiceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> postDigitalServiceInputsVirtualEquipmentsRest(final String subscriber, final Long organization, final String digitalServiceUid, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        digitalServiceService.updateLastUpdateDate(digitalServiceUid);
        return new ResponseEntity<>(inVirtualEquipmentService.createInVirtualEquipmentDigitalService(digitalServiceUid, inVirtualEquipmentRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> getDigitalServiceInputsVirtualEquipmentsRest(String subscriber,
                                                                                                     Long organization,
                                                                                                     String digitalServiceUid) {

        return ResponseEntity.ok().body(inVirtualEquipmentService.getByDigitalService(digitalServiceUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> getDigitalServiceInputsVirtualEquipmentRest(String subscriber,
                                                                                              Long organization,
                                                                                              String digitalServiceUid,
                                                                                              Long id) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceInputsVirtualEquipmentRest(final String subscriber, final Long organization, final String digitalServiceUid, final Long id) {
        digitalServiceService.updateLastUpdateDate(digitalServiceUid);
        inVirtualEquipmentService.deleteInVirtualEquipment(digitalServiceUid, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> putDigitalServiceInputsVirtualEquipmentRest(final String subscriber,
                                                                                              final Long organization, final String digitalServiceUid, final Long id,
                                                                                              final InVirtualEquipmentRest inVirtualEquipmentRest) {
        digitalServiceService.updateLastUpdateDate(digitalServiceUid);
        return new ResponseEntity<>(inVirtualEquipmentService.updateInVirtualEquipment(digitalServiceUid, id, inVirtualEquipmentRest), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> updateDigitalServiceInputsVirtualEquipmentRest(String subscriber, Long organization,
                                                                                                       String digitalServiceUid, Long physicalEqpId,
                                                                                                       List<InVirtualEquipmentRest> inVirtualEquipmentRest) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceUid, physicalEqpId, inVirtualEquipmentRest));
    }
}
