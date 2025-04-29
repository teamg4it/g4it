/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiworkspace.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorOrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiworkspace.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiworkspace.mapper.SubscriberDetailsRestMapper;
import com.soprasteria.g4it.backend.server.gen.api.WorkspaceApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.SubscriberDetailsRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserDetailsRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Workspace Rest Service.
 */
@Service
public class WorkspaceController implements WorkspaceApiDelegate {
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
    AdministratorOrganizationService administratorOrganizationService;

    @Autowired
    private OrganizationRestMapper organizationRestMapper;

    @Autowired
    SubscriberDetailsRestMapper subscriberDetailsRestMapper;

    @Autowired
    WorkspaceService workspaceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<OrganizationRest> createWorkspace(OrganizationUpsertRest organizationUpsertRest) {
        return new ResponseEntity<>(organizationRestMapper.toDto(administratorOrganizationService.createOrganization(organizationUpsertRest, authService.getAdminUser(), false)),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<SubscriberDetailsRest>> getDomainSubscribers(UserDetailsRest userDetailsRest) {
        return new ResponseEntity<>(
                subscriberDetailsRestMapper.toDto(this.workspaceService.searchSubscribersByDomainName(userDetailsRest.getEmail())), HttpStatus.OK);
    }
}
