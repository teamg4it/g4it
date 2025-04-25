/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.controller;

import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorOrganizationService;
import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorRoleService;
import com.soprasteria.g4it.backend.apiadministrator.business.AdministratorService;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.RoleRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.SubscriberRestMapper;
import com.soprasteria.g4it.backend.apiuser.mapper.UserRestMapper;
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
    AdministratorOrganizationService administratorOrganizationService;

    @Autowired
    AdministratorRoleService administratorRoleService;
    @Autowired
    AuthService authService;
    @Autowired
    UserRestMapper userRestMapper;
    @Autowired
    private SubscriberRestMapper subscriberRestMapper;
    @Autowired
    private OrganizationRestMapper organizationRestMapper;

    @Autowired
    private RoleRestMapper roleRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<SubscriberRest>> getSubscribers() {
        return ResponseEntity.ok(
                subscriberRestMapper.toDto(this.administratorService.getSubscribers(authService.getAdminUser())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<SubscriberRest> updateSubscriber(final Long subscriberId, final CriteriaRest criteriaRest) {
        return ResponseEntity.ok(subscriberRestMapper.toDto(this.administratorService.updateSubscriberCriteria(subscriberId, criteriaRest, authService.getUser()))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<SubscriberRest>> getOrganizations(final Long organizationId, final Long subscriberId) {
        return new ResponseEntity<>(subscriberRestMapper.toDto(administratorOrganizationService.getOrganizations(subscriberId, organizationId, authService.getUser())),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<OrganizationRest> createOrganization(final OrganizationUpsertRest organizationUpsertRest) {
        return new ResponseEntity<>(organizationRestMapper.toDto(administratorOrganizationService.createOrganization(organizationUpsertRest, authService.getAdminUser(), true)),
                HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<OrganizationRest> updateOrganization(final OrganizationUpsertRest organizationUpsertRest, final Long organizationId) {
        return new ResponseEntity<>(organizationRestMapper.toDto(administratorOrganizationService.updateOrganization(organizationId, organizationUpsertRest, authService.getUser())),
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
    public ResponseEntity<List<UserInfoRest>> getUsersOfOrg(Long organizationId) {
        return new ResponseEntity<>
                (userRestMapper.toListRest(administratorOrganizationService.getUsersOfOrg(organizationId, authService.getUser())), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserInfoRest>> linkUserToOrg(final LinkUserRoleRest linkUserRoleRest) {
        return new ResponseEntity<>
                (userRestMapper.toListRest(administratorOrganizationService.linkUserToOrg(linkUserRoleRest, authService.getUser(), true)), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserInfoRest>> updateRoleAccess(final LinkUserRoleRest linkUserRoleRest) {
        return new ResponseEntity<>(
                userRestMapper.toListRest(administratorOrganizationService.linkUserToOrg(linkUserRoleRest, authService.getUser(), true)), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserSearchRest>> searchUserByName(final String searchedName, final Long subscriberId, final Long organizationId) {
        return new ResponseEntity<>(
                userRestMapper.toRestObj(this.administratorService.searchUserByName(searchedName, subscriberId, organizationId, authService.getUser())), HttpStatus.OK);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteUserOrgLink(final LinkUserRoleRest linkUserRoleRest) {
        administratorOrganizationService.deleteUserOrgLink(linkUserRoleRest, authService.getUser());
        return ResponseEntity.noContent().<Void>build();
    }

}

