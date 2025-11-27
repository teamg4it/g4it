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
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceVersionRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceVersionBO;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.common.utils.AuthorizationUtils;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceShareRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceVersionRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDigitalServiceVersionRest;
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
    public ResponseEntity<List<DigitalServiceRest>> getDigitalServices(final String organization, final Long workspace, final Boolean isAi) {
        if (isAi != null && isAi) {
            authorizationUtils.checkEcomindAuthorization();
            authorizationUtils.checkEcomindEnabledForOrganization(organization);
        }
        final List<DigitalServiceBO> digitalServiceBOs = digitalServiceService.getDigitalServices(workspace, isAi);
        return ResponseEntity.ok(digitalServiceRestMapper.toDto(digitalServiceBOs));
    }

}


