/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceVersionService;
import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceInputsVirtualEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceVersionInputsVirtualEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
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
public class InVirtualEquipmentDigitalServiceVersionController implements DigitalServiceVersionInputsVirtualEquipmentApiDelegate {

    /**
     * Service to access virtual equipment input data.
     */
    private InVirtualEquipmentService inVirtualEquipmentService;
    private DigitalServiceVersionService digitalServiceVersionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> postDigitalServiceVersionInputsVirtualEquipmentsRest(final String organization, final Long workspace, final String digitalServiceVersionUid, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return new ResponseEntity<>(inVirtualEquipmentService.createInVirtualEquipmentDigitalServiceVersion(digitalServiceVersionUid, inVirtualEquipmentRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> getDigitalServiceVersionInputsVirtualEquipmentsRest(String organization,
                                                                                                     Long workspace,
                                                                                                     String digitalServiceVersionUid) {

        return ResponseEntity.ok().body(inVirtualEquipmentService.getByDigitalServiceVersion(digitalServiceVersionUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> getDigitalServiceVersionInputsVirtualEquipmentRest(String organization,
                                                                                              Long workspace,
                                                                                              String digitalServiceVersionUid,
                                                                                              Long id) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.getByDigitalServiceVersionAndId(digitalServiceVersionUid, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceVersionInputsVirtualEquipmentRest(final String organization, final Long workspace, final String digitalServiceVersionUid, final Long id) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        inVirtualEquipmentService.deleteInVirtualEquipment(digitalServiceVersionUid, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> putDigitalServiceVersionInputsVirtualEquipmentRest(final String organization,
                                                                                              final Long workspace, final String digitalServiceVersionUid, final Long id,
                                                                                              final InVirtualEquipmentRest inVirtualEquipmentRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return new ResponseEntity<>(inVirtualEquipmentService.updateInVirtualEquipment(digitalServiceVersionUid, id, inVirtualEquipmentRest), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> updateDigitalServiceVersionInputsVirtualEquipmentRest(String organization, Long workspace,
                                                                                                       String digitalServiceVersionUid, Long physicalEqpId,
                                                                                                       List<InVirtualEquipmentRest> inVirtualEquipmentRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return ResponseEntity.ok().body(inVirtualEquipmentService.updateOrDeleteInVirtualEquipments(digitalServiceVersionUid, physicalEqpId, inVirtualEquipmentRest));
    }
}
