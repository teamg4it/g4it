/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceVersionService;
import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceVersionInputsPhysicalEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Digital Service Input Physical Equipment Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InPhysicalEquipmentDigitalServiceVersionController implements DigitalServiceVersionInputsPhysicalEquipmentApiDelegate {

    /**
     * Service to access physical equipment input data.
     */
    private InPhysicalEquipmentService inPhysicalEquipmentService;
    private DigitalServiceVersionService digitalServiceVersionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postDigitalServiceVersionInputsPhysicalEquipmentsRest(final String organization, final Long workspace, final String digitalServiceVersionUid, final InPhysicalEquipmentRest inPhysicalEquipmentRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return new ResponseEntity<>(inPhysicalEquipmentService.createInPhysicalEquipmentDigitalServiceVersion(digitalServiceVersionUid, inPhysicalEquipmentRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InPhysicalEquipmentRest>> getDigitalServiceVersionInputsPhysicalEquipmentsRest(String organization,
                                                                                                              Long workspace,
                                                                                                              String digitalServiceVersionUid) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByDigitalServiceVersion(digitalServiceVersionUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> getDigitalServiceVersionInputsPhysicalEquipmentRest(String organization,
                                                                                                       Long workspace,
                                                                                                       String digitalServiceUid,
                                                                                                       Long id) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByDigitalServiceVersionAndId(digitalServiceUid, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceVersionInputsPhysicalEquipmentRest(final String organization, final Long workspace, final String digitalServiceUid, final Long id) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceUid);
        inPhysicalEquipmentService.deleteInPhysicalEquipment(digitalServiceUid, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> putDigitalServiceVersionInputsPhysicalEquipmentRest(final String organization,
                                                                                                       final Long workspace, final String digitalServiceVersionUid, final Long id,
                                                                                                       final InPhysicalEquipmentRest inPhysicalEquipmentRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return new ResponseEntity<>(inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceVersionUid, id, inPhysicalEquipmentRest), HttpStatus.OK);
    }
}
