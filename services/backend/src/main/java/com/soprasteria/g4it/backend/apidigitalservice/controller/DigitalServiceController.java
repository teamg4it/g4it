/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceService;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

/**
 * Digital Service endpoints.
 */
@Service
public class DigitalServiceController implements DigitalServiceApiDelegate {

    /**
     * Auth Service.
     */
    @Autowired
    private AuthService authService;

    /**
     * Digital Service.
     */
    @Autowired
    private DigitalServiceService digitalServiceService;

    @Autowired
    private UserService userService;

    /**
     * DigitalServiceRest Mapper.
     */
    @Autowired
    private DigitalServiceRestMapper digitalServiceRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceRest> createDigitalService(final String subscriber, final Long organization) {
        final DigitalServiceBO digitalServiceBO = digitalServiceService.createDigitalService(organization, authService.getUser().getId());
        final DigitalServiceRest digitalServiceDTO = digitalServiceRestMapper.toDto(digitalServiceBO);
        return ResponseEntity.created(URI.create("/".concat(String.join("/", organization.toString(), "digital-services", digitalServiceBO.getUid())))).body(digitalServiceDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceRest>> getDigitalServices(final String subscriber, final Long organization) {
        final List<DigitalServiceBO> digitalServiceBOs = digitalServiceService.getDigitalServices(organization);
        return ResponseEntity.ok(digitalServiceRestMapper.toDto(digitalServiceBOs));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalService(final String subscriber, final Long organization, final String digitalServiceUid) {
        digitalServiceService.deleteDigitalService(digitalServiceUid);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceRest> getDigitalService(final String subscriber,
                                                                final Long organization,
                                                                final String digitalServiceUid) {
        return ResponseEntity.ok(digitalServiceRestMapper.toDto(digitalServiceService.getDigitalService(digitalServiceUid)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceRest> updateDigitalService(final String subscriber,
                                                                   final Long organization,
                                                                   final String digitalServiceUid,
                                                                   final DigitalServiceRest digitalService) {
        digitalService.setUid(digitalServiceUid);
        return ResponseEntity.ok(digitalServiceRestMapper.toDto(digitalServiceService.updateDigitalService(
                digitalServiceRestMapper.toBusinessObject(digitalService),
                authService.getUser()
        )));
    }

}


