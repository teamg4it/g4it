/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.controller;

import com.soprasteria.g4it.backend.apidigitalservice.business.DigitalServiceVersionService;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceVersionRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.common.utils.AuthorizationUtils;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceVersionApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceShareRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDigitalServiceVersionRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Digital Service endpoints.
 */
@Service
public class DigitalServiceVersionController implements DigitalServiceVersionApiDelegate {

    /**
     * Auth Service.
     */
    @Autowired
    private AuthService authService;

    /**
     * Digital Service.
     */
    @Autowired
    private DigitalServiceVersionService digitalServiceVersionService;

    /**
     * DigitalServiceRest Mapper.
     */
    @Autowired
    private DigitalServiceRestMapper digitalServiceRestMapper;
    /**
     * DigitalServiceRest Mapper.
     */
    @Autowired
    private DigitalServiceVersionRestMapper digitalServiceVersionRestMapper;

    @Autowired
    private AuthorizationUtils authorizationUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceVersionRest> createDigitalServiceVersion(final String organization,
                                                                                 final Long workspace,
                                                                                 final InDigitalServiceVersionRest inDigitalServiceVersionRest
    ) {
        if (inDigitalServiceVersionRest.getIsAi() != null && inDigitalServiceVersionRest.getIsAi()) {
            authorizationUtils.checkEcomindAuthorization();
            authorizationUtils.checkEcomindEnabledForOrganization(organization);
        }

        final DigitalServiceVersionBO digitalServiceVersionBO = digitalServiceVersionService.createDigitalServiceVersion(
                workspace,
                authService.getUser().getId(),
                inDigitalServiceVersionRest
        );

        DigitalServiceVersionRest digitalServiceVersionDTO = digitalServiceVersionRestMapper.toDto(digitalServiceVersionBO);
        return ResponseEntity.created(URI.create("/".concat(String.join("/", workspace.toString(), "digital-service-version", digitalServiceVersionBO.getUid())))).body(digitalServiceVersionDTO);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceVersion(final String organization, final Long workspace, final String digitalServiceVersionUid) {
        digitalServiceVersionService.deleteDigitalServiceVersion(digitalServiceVersionUid);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceVersionRest> getDigitalServiceVersion(final String organization,
                                                                              final Long workspace,
                                                                              final String digitalServiceVersionUid) {
        return ResponseEntity.ok(digitalServiceVersionRestMapper.toDto(digitalServiceVersionService.getDigitalServiceVersion(digitalServiceVersionUid)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceVersionRest> updateDigitalServiceVersion(final String organization,
                                                                                 final Long workspace,
                                                                                 final String digitalServiceVersionUid,
                                                                                 final DigitalServiceVersionRest digitalServiceVersion) {
        digitalServiceVersion.setUid(digitalServiceVersionUid);
        return ResponseEntity.ok(digitalServiceVersionRestMapper.toDto(digitalServiceVersionService.updateDigitalServiceVersion(
                digitalServiceVersionRestMapper.toBusinessObject(digitalServiceVersion), organization, workspace,
                authService.getUser()
        )));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DigitalServiceShareRest> shareDigitalServiceVersion(final String organization,
                                                                              final Long workspace,
                                                                              final String digitalServiceVersionUid,
                                                                              final Boolean extendLink) {
        return ResponseEntity.ok(digitalServiceVersionService.shareDigitalService(organization, workspace, digitalServiceVersionUid,
                authService.getUser(), extendLink));
    }

}


