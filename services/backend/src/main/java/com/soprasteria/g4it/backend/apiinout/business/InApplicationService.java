/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apiinout.mapper.InApplicationMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InApplicationRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class InApplicationService {

    private InApplicationRepository inApplicationRepository;
    private InApplicationMapper inApplicationMapper;
    private InventoryRepository inventoryRepository;

    /**
     * Get the applications list linked to an inventory
     *
     * @param inventoryId the inventory id
     * @return the application list.
     */
    public List<InApplicationRest> getByInventory(final Long inventoryId) {
        final List<InApplication> inApplication = inApplicationRepository.findByInventoryId(inventoryId);
        return inApplicationMapper.toRest(inApplication);
    }

    /**
     * Retrieving an application for an inventory and an application id.
     *
     * @param inventoryId the inventory id
     * @param id          the application id
     * @return InApplicationRest
     */
    public InApplicationRest getByInventoryAndId(final Long inventoryId, Long id) {
        final Optional<InApplication> inApplication = inApplicationRepository.findByInventoryIdAndId(inventoryId, id);
        if (inApplication.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no application with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inApplication.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this application id: %d", inventoryId, inApplication.get().getDigitalServiceVersionUid(), id));
        }

        return inApplicationMapper.toRest(inApplication.get());
    }

    /**
     * Create a new in application for a specific inventory.
     *
     * @param inventoryId       the inventory id.
     * @param inApplicationRest the inApplicationRest.
     * @return the business object corresponding on application created.
     */
    public InApplicationRest createInApplicationInventory(final Long inventoryId, final InApplicationRest inApplicationRest) {
        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);

        if (inventory.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory of id : %s, doesn't exist", inventoryId));
        }

        final InApplication inApplicationToCreate = inApplicationMapper.toEntity(inApplicationRest);
        final LocalDateTime now = LocalDateTime.now();
        inApplicationToCreate.setInventoryId(inventoryId);
        inApplicationToCreate.setCreationDate(now);
        inApplicationToCreate.setLastUpdateDate(now);

        inApplicationRepository.save(inApplicationToCreate);
        return inApplicationMapper.toRest(inApplicationToCreate);
    }

    /**
     * Update an application.
     *
     * @param inventoryId             the inventory id.
     * @param id                      the application's id
     * @param inApplicationUpdateRest the inApplicationUpdateRest.
     * @return InventoryRest
     */
    public InApplicationRest updateInApplication(final Long inventoryId, final Long id, final InApplicationRest inApplicationUpdateRest) {
        final Optional<InApplication> inApplication = inApplicationRepository.findByInventoryIdAndId(inventoryId, id);
        if (inApplication.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no application with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inApplication.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this application id: %d", inventoryId, inApplication.get().getInventoryId(), id));
        }

        final InApplication objectToUpdate = inApplication.get();
        final InApplication updates = inApplicationMapper.toEntity(inApplicationUpdateRest);
        inApplicationMapper.merge(objectToUpdate, updates);

        inApplicationRepository.save(objectToUpdate);
        return inApplicationMapper.toRest(objectToUpdate);
    }

    /**
     * Delete an application
     *
     * @param id the application id.
     */
    public void deleteInApplication(final Long id) {
        inApplicationRepository.deleteById(id);
    }

}

