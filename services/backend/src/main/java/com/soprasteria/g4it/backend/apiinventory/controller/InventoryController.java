/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.controller;

import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.mapper.InventoryRestMapper;
import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryUpdateRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Inventory Service.
 */
@Slf4j
@Service
@NoArgsConstructor
public class InventoryController implements InventoryApiDelegate {

    /**
     * User Service
     */
    @Autowired
    UserService userService;
    /**
     * Auth Service
     */
    @Autowired
    AuthService authService;
    /**
     * Service to access inventory data.
     */
    @Autowired
    private InventoryService inventoryService;
    /**
     * Service to delete inventory data.
     */
    @Autowired
    private InventoryDeleteService inventoryDeleteService;
    /**
     * InventoryRest Mapper
     */
    @Autowired
    private InventoryRestMapper inventoryRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InventoryRest>> getInventories(final String organization, final Long workspace, final Long inventoryId) {
        return ResponseEntity.ok().body(inventoryRestMapper.toRest(this.inventoryService.getInventories(workspace, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventories(final String organization, final Long workspace) {
        log.info("Start Delete inventory for workspace {}", workspace);
        inventoryDeleteService.deleteInventories(organization, workspace);
        log.info("End Delete inventory for workspace {}", workspace);
        return ResponseEntity.noContent().<Void>build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventory(final String organization,
                                                final Long workspace,
                                                final Long inventoryId) {
        log.info("Start Delete inventory on workspaceId={} - inventoryId={}", workspace, inventoryId);
        inventoryDeleteService.deleteInventory(organization, workspace, inventoryId);
        log.info("End Delete inventory on workspaceId={} - inventoryId={}", workspace, inventoryId);
        return ResponseEntity.noContent().<Void>build();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InventoryRest> createInventory(final String organization,
                                                         final Long workspace,
                                                         final InventoryCreateRest inventoryCreateRest) {
        return new ResponseEntity<>(inventoryRestMapper.toDto(this.inventoryService.createInventory(organization, workspace, inventoryCreateRest, authService.getUser())), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InventoryRest> updateInventory(final String organization,
                                                         final Long workspace,
                                                         final InventoryUpdateRest inventoryUpdateRest) {
        return new ResponseEntity<>(inventoryRestMapper.toDto(this.inventoryService.updateInventory(organization, workspace, inventoryUpdateRest, authService.getUser())), HttpStatus.OK);
    }

}
