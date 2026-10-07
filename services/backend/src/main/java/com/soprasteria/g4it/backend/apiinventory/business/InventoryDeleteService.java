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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InventoryDeleteService {

    /**
     * Repository to access inventory data.
     */
    
    private final InventoryRepository inventoryRepository;

    /**
     * The workspace service.
     */
    
    private final WorkspaceService workspaceService;

    /**
     * Inventory Indicator Service
     */
    
    private final InventoryIndicatorService inventoryIndicatorService;
    
    private final InDatacenterRepository inDatacenterRepository;
    
    private final InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    
    private final InVirtualEquipmentRepository inVirtualEquipmentRepository;
    
    private final InApplicationRepository inApplicationRepository;


    /**
     * Delete all inventories in a workspace.
     *
     * @param organizationName the client organization name.
     * @param workspaceId the linked workspace id.
     */
    public void deleteInventories(final String organizationName, final Long workspaceId) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);
        inventoryRepository.findByWorkspace(linkedWorkspace)
                .forEach(inventory -> deleteInventory(organizationName, workspaceId, inventory));
    }


    /**
     * Delete an inventory for a workspace on a date.
     *
     * @param organizationName the client organization name.
     * @param workspaceId the workspace id.
     * @param inventoryId    the inventory id.
     */
    public void deleteInventory(final String organizationName, final Long workspaceId, final Long inventoryId) {
        final Workspace linkedWorkspace = workspaceService.getWorkspaceById(workspaceId);
        inventoryRepository.findByWorkspaceAndId(linkedWorkspace, inventoryId)
                .ifPresent(inventory -> deleteInventory(organizationName, workspaceId, inventory));
    }


    /**
     * Delete the inventory based on the inventory database object
     *
     * @param organizationName the client organization name.
     * @param workspaceId the workspace id.
     * @param inventory      the inventory database object.
     */

    public void deleteInventory(final String organizationName, final Long workspaceId, final Inventory inventory) {
        Long inventoryId = inventory.getId();
        // Delete input data
        inDatacenterRepository.deleteByInventoryId(inventoryId);
        inPhysicalEquipmentRepository.deleteByInventoryId(inventoryId);
        inVirtualEquipmentRepository.deleteByInventoryId(inventoryId);
        inApplicationRepository.deleteByInventoryId(inventoryId);

        // Delete EVALUATING tasks and indicator data
        inventoryIndicatorService.deleteIndicators(organizationName, workspaceId, inventoryId);

        // Remove inventory.
        inventoryRepository.deleteByInventoryId(inventory.getId());
    }

}
