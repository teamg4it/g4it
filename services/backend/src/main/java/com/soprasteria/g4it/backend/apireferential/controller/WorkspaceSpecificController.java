/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.controller;

import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetService;
import com.soprasteria.g4it.backend.server.gen.api.WorkspaceSpecificApiDelegate;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Workspace specific endpoints
 */
@Service
@NoArgsConstructor
public class WorkspaceSpecificController implements WorkspaceSpecificApiDelegate {

    @Autowired
    private ReferentialGetService referentialGetService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Boolean> isWorkspaceSpecific(final String organization, final Long workspace) {
        final boolean result = referentialGetService.isWorkspaceSpecific(workspace);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
