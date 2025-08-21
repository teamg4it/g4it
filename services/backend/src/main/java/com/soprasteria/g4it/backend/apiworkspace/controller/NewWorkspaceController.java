/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorWorkspaceService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.WorkspaceRestMapper;
import com.soprasteria.g4it.backend.apiworkspace.business.NewWorkspaceService;
import com.soprasteria.g4it.backend.apiworkspace.mapper.SubscriberDetailsRestMapper;
import com.soprasteria.g4it.backend.server.gen.api.NewWorkspaceApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.SubscriberDetailsRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserDetailsRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.WorkspaceUpdateRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Workspace Rest Service.
 */
@Service
public class NewWorkspaceController implements NewWorkspaceApiDelegate {
    /**
     * Auth Service.
     */
    @Autowired
    private AuthService authService;

    /**
     * UserRest Mapper.
     */
    @Autowired
    private UserRestMapper userRestMapper;

    @Autowired
    AdministratorWorkspaceService administratorWorkspaceService;

    @Autowired
    private WorkspaceRestMapper workspaceRestMapper;

    @Autowired
    SubscriberDetailsRestMapper subscriberDetailsRestMapper;

    @Autowired
    NewWorkspaceService newWorkspaceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<WorkspaceRest> createWorkspace(WorkspaceUpdateRest organizationUpsertRest) {
        return new ResponseEntity<>(workspaceRestMapper.toDto(administratorWorkspaceService.createWorkspace(organizationUpsertRest, authService.getAdminUser(), false)),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<SubscriberDetailsRest>> getDomainSubscribers(UserDetailsRest userDetailsRest) {
        return new ResponseEntity<>(
                subscriberDetailsRestMapper.toDto(this.newWorkspaceService.searchSubscribersByDomainName(userDetailsRest.getEmail())), HttpStatus.OK);
    }
}
