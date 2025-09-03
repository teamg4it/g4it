/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.business;

import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.WorkspaceService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class InventoryDeleteService {

    /**
     * Repository to access inventory data.
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * The organization service.
     */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Inventory Indicator Service
     */
    @Autowired
    private InventoryIndicatorService inventoryIndicatorService;
    @Autowired
    private InDatacenterRepository inDatacenterRepository;
    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    private InApplicationRepository inApplicationRepository;


    /**
     * Delete all inventories in an organization.
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the linked organization's id.
     */
    public void deleteInventories(final String subscriberName, final Long organizationId) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(organizationId);
        inventoryRepository.findByWorkspace(linkedWorkspace)
                .forEach(inventory -> deleteInventory(subscriberName, organizationId, inventory));
    }


    /**
     * Delete an inventory for an organization on a date.
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the organization id.
     * @param inventoryId    the inventory id.
     */
    public void deleteInventory(final String subscriberName, final Long organizationId, final Long inventoryId) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(organizationId);
        inventoryRepository.findByWorkspaceAndId(linkedWorkspace, inventoryId)
                .ifPresent(inventory -> deleteInventory(subscriberName, organizationId, inventory));
    }


    /**
     * Delete the inventory based on the inventory database object
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the organization's id.
     * @param inventory      the inventory database object.
     */

    public void deleteInventory(final String subscriberName, final Long organizationId, final Inventory inventory) {
        Long inventoryId = inventory.getId();
        // Delete input data
        inDatacenterRepository.deleteByInventoryId(inventoryId);
        inPhysicalEquipmentRepository.deleteByInventoryId(inventoryId);
        inVirtualEquipmentRepository.deleteByInventoryId(inventoryId);
        inApplicationRepository.deleteByInventoryId(inventoryId);

        // Delete EVALUATING tasks and indicator data
        inventoryIndicatorService.deleteIndicators(subscriberName, organizationId, inventoryId);

        // Remove inventory.
        inventoryRepository.deleteByInventoryId(inventory.getId());
    }

}
