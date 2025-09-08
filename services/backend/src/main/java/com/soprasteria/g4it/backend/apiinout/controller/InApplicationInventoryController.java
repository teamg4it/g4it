/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.InApplicationService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryInputsApplicationApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Inventory Input Application Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InApplicationInventoryController implements InventoryInputsApplicationApiDelegate {

    /**
     * Service to access application input data.
     */
    private InApplicationService inApplicationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InApplicationRest> postInventoryInputsApplicationsRest(final String organization, final Long workspace, final Long inventoryId, final InApplicationRest inApplicationRest) {
        return new ResponseEntity<>(inApplicationService.createInApplicationInventory(inventoryId, inApplicationRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InApplicationRest>> getInventoryInputsApplicationsRest(String organization,
                                                                                      Long workspace,
                                                                                      Long inventoryId) {
        return ResponseEntity.ok().body(inApplicationService.getByInventory(inventoryId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InApplicationRest> getInventoryInputsApplicationRest(String organization,
                                                                               Long workspace,
                                                                               Long inventoryId,
                                                                               Long id) {
        return ResponseEntity.ok().body(inApplicationService.getByInventoryAndId(inventoryId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventoryInputsApplicationRest(final String organization, final Long workspace, final Long inventoryId, final Long id) {
        inApplicationService.deleteInApplication(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InApplicationRest> putInventoryInputsApplicationRest(final String organization,
                                                                               final Long workspace, final Long inventoryId, final Long id,
                                                                               final InApplicationRest inApplicationRest) {
        return new ResponseEntity<>(inApplicationService.updateInApplication(inventoryId, id, inApplicationRest), HttpStatus.OK);
    }

}
