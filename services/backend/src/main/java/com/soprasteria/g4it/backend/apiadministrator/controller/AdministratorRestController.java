/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorRoleService;
import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorService;
import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorWorkspaceService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.RoleRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.WorkspaceRestMapper;
import com.soprasteria.g4it.backend.server.gen.api.AdministratorApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Administrator Rest Service.
 */
@Slf4j
@Service
@NoArgsConstructor
public class AdministratorRestController implements AdministratorApiDelegate {

    @Autowired
    AdministratorService administratorService;

    @Autowired
    AdministratorWorkspaceService administratorWorkspaceService;

    @Autowired
    AdministratorRoleService administratorRoleService;
    @Autowired
    AuthService authService;
    @Autowired
    UserRestMapper userRestMapper;
    @Autowired
    private OrganizationRestMapper organizationRestMapper;
    @Autowired
    private WorkspaceRestMapper workspaceRestMapper;
    @Autowired
    private RoleRestMapper roleRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OrganizationRest>> getOrganizations() {
        return ResponseEntity.ok(
                organizationRestMapper.toDto(this.administratorService.getOrganizations(authService.getAdminUser())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<OrganizationRest> getOrganizationById(final Long organizationId) {
        return ResponseEntity.ok(
                organizationRestMapper.toDto(this.administratorService.getOrganizationById(organizationId)));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<OrganizationRest> updateOrganization(final Long organizationId, final CriteriaRest criteriaRest) {
        return ResponseEntity.ok(organizationRestMapper.toDto(this.administratorService.updateOrganizationCriteria(organizationId, criteriaRest, authService.getUser()))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OrganizationRest>> getWorkspaces(final Long workspaceId, final Long organizationId) {
        return new ResponseEntity<>(organizationRestMapper.toDto(administratorWorkspaceService.getWorkspaces(organizationId, workspaceId, authService.getUser())),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<WorkspaceRest> createWorkspaceAsAdmin(final WorkspaceUpsertRest workspaceUpsertRest) {
        return new ResponseEntity<>(workspaceRestMapper.toDto(administratorWorkspaceService.createWorkspace(workspaceUpsertRest, authService.getAdminUser(), true)),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<WorkspaceRest> updateWorkspace(final WorkspaceUpsertRest workspaceUpsertRest, final Long workspaceId) {
        return new ResponseEntity<>(workspaceRestMapper.toDto(administratorWorkspaceService.updateWorkspace(workspaceId, workspaceUpsertRest, authService.getUser())),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RolesRest>> getRoles() {
        return new ResponseEntity<>(
                roleRestMapper.toDto(this.administratorRoleService.getAllRoles(authService.getUser())), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserInfoRest>> getUsersOfWorkspace(Long workspaceId) {
        return new ResponseEntity<>
                (userRestMapper.toListRest(administratorWorkspaceService.getUsersOfWorkspace(workspaceId, authService.getUser())), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserInfoRest>> linkUserToWorkspace(final LinkUserRoleRest linkUserRoleRest) {
        return new ResponseEntity<>
                (userRestMapper.toListRest(administratorWorkspaceService.linkUserToWorkspace(linkUserRoleRest, authService.getUser(), true)), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserInfoRest>> updateRoleAccess(final LinkUserRoleRest linkUserRoleRest) {
        return new ResponseEntity<>(
                userRestMapper.toListRest(administratorWorkspaceService.linkUserToWorkspace(linkUserRoleRest, authService.getUser(), true)), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserSearchRest>> searchUserByName(final String searchedName, final Long organizationId, final Long workspaceId) {
        return new ResponseEntity<>(
                userRestMapper.toRestObj(this.administratorService.searchUserByName(searchedName, organizationId, workspaceId, authService.getUser())), HttpStatus.OK);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteUserWorkspaceLink(final LinkUserRoleRest linkUserRoleRest) {
        administratorWorkspaceService.deleteUserWorkLink(linkUserRoleRest, authService.getUser());
        return ResponseEntity.noContent().<Void>build();
    }

}

