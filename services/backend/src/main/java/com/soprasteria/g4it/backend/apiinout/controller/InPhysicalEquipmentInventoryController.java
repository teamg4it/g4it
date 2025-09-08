/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryInputsPhysicalEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Inventory Input Physical Equipment Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InPhysicalEquipmentInventoryController implements InventoryInputsPhysicalEquipmentApiDelegate {

    /**
     * Service to access physical equipment input data.
     */
    private InPhysicalEquipmentService inPhysicalEquipmentService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postInventoryInputsPhysicalEquipmentsRest(final String organization, final Long workspace, final Long inventoryId, final InPhysicalEquipmentRest inPhysicalEquipmentRest) {
        return new ResponseEntity<>(inPhysicalEquipmentService.createInPhysicalEquipmentInventory(inventoryId, inPhysicalEquipmentRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InPhysicalEquipmentRest>> getInventoryInputsPhysicalEquipmentsRest(String organization,
                                                                                                  Long workspace,
                                                                                                  Long inventoryId) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByInventory(inventoryId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> getInventoryInputsPhysicalEquipmentRest(String organization,
                                                                                           Long workspace,
                                                                                           Long inventoryId,
                                                                                           Long id) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByInventoryAndId(inventoryId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventoryInputsPhysicalEquipmentRest(final String organization, final Long workspace, final Long inventoryId, final Long id) {
        inPhysicalEquipmentService.deleteInPhysicalEquipment(inventoryId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> putInventoryInputsPhysicalEquipmentRest(final String organization,
                                                                                           final Long workspace, final Long inventoryId, final Long id,
                                                                                           final InPhysicalEquipmentRest inPhysicalEquipmentRest) {
        return new ResponseEntity<>(inPhysicalEquipmentService.updateInPhysicalEquipment(inventoryId, id, inPhysicalEquipmentRest), HttpStatus.OK);
    }
}
