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
import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceVersionInputsDatacenterApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * DigitalService Input Datacenter Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InDatacenterDigitalServiceVersionController implements DigitalServiceVersionInputsDatacenterApiDelegate {

    /**
     * Service to access datacenter input data.
     */
    private InDatacenterService inDatacenterService;
    private DigitalServiceVersionService digitalServiceVersionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> postDigitalServiceVersionInputsDatacentersRest(final String organization, final Long workspace, final String digitalServiceVersionUid, final InDatacenterRest inDatacenterRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return new ResponseEntity<>(inDatacenterService.createInDatacenterDigitalServiceVersion(digitalServiceVersionUid, inDatacenterRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InDatacenterRest>> getDigitalServiceVersionInputsDatacentersRest(String organization,
                                                                                         Long workspace,
                                                                                         String digitalServiceVersionUid) {
        return ResponseEntity.ok().body(inDatacenterService.getByDigitalServiceVersion(digitalServiceVersionUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> getDigitalServiceVersionInputsDatacenterRest(String organization,
                                                                                  Long workspace,
                                                                                  String digitalServiceVersionUid,
                                                                                  Long id) {
        return ResponseEntity.ok().body(inDatacenterService.getByDigitalServiceVersionAndId(digitalServiceVersionUid, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceVersionInputsDatacenterRest(final String organization, final Long workspace, final String digitalServiceVersionUid, final Long id) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        inDatacenterService.deleteInDatacenter(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> putDigitalServiceVersionInputsDatacenterRest(final String organization,
                                                                                  final Long workspace, final String digitalServiceVersionUid, final Long id,
                                                                                  final InDatacenterRest inDatacenterRest) {
        digitalServiceVersionService.updateLastUpdateDate(digitalServiceVersionUid);
        return new ResponseEntity<>(inDatacenterService.updateInDatacenter(digitalServiceVersionUid, id, inDatacenterRest), HttpStatus.OK);
    }

}
