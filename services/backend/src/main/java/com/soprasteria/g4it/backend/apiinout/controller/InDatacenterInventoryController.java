/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryInputsDatacenterApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Inventory Input Datacenter Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InDatacenterInventoryController implements InventoryInputsDatacenterApiDelegate {

    /**
     * Service to access datacenter input data.
     */
    private InDatacenterService inDatacenterService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> postInventoryInputsDatacentersRest(final String organization, final Long workspace, final Long inventoryId, final InDatacenterRest inDatacenterRest) {
        return new ResponseEntity<>(inDatacenterService.createInDatacenterInventory(inventoryId, inDatacenterRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InDatacenterRest>> getInventoryInputsDatacentersRest(String organization,
                                                                                    Long workspace,
                                                                                    Long inventoryId) {
        return ResponseEntity.ok().body(inDatacenterService.getByInventory(inventoryId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> getInventoryInputsDatacenterRest(String organization,
                                                                             Long workspace,
                                                                             Long inventoryId,
                                                                             Long id) {
        return ResponseEntity.ok().body(inDatacenterService.getByInventoryAndId(inventoryId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventoryInputsDatacenterRest(final String organization, final Long workspace, final Long inventoryId, final Long id) {
        inDatacenterService.deleteInDatacenter(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> putInventoryInputsDatacenterRest(final String organization,
                                                                             final Long workspace, final Long inventoryId, final Long id,
                                                                             final InDatacenterRest inDatacenterRest) {
        return new ResponseEntity<>(inDatacenterService.updateInDatacenter(inventoryId, id, inDatacenterRest), HttpStatus.OK);
    }

}
