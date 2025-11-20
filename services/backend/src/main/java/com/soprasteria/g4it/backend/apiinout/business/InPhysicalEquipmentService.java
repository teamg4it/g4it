/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InPhysicalEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
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
public class InPhysicalEquipmentService {

    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    private InPhysicalEquipmentMapper inPhysicalEquipmentMapper;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private InventoryRepository inventoryRepository;

    /**
     * Get the physical equipments list linked to a digital service.
     *
     * @param digitalServiceVersionUid the digital service UID.
     * @return the physical equipment list.
     */
    public List<InPhysicalEquipmentRest> getByDigitalServiceVersion(final String digitalServiceVersionUid) {
        final List<InPhysicalEquipment> inPhysicalEquipments = inPhysicalEquipmentRepository.findByDigitalServiceVersionUidOrderByName(digitalServiceVersionUid);
        return inPhysicalEquipmentMapper.toRest(inPhysicalEquipments);
    }

    /**
     * Retrieving a physical equipment for a digital service and a physical equipment id.
     *
     * @param digitalServiceVersionUid the digital service UID.
     * @param id                the physical equipment id
     * @return InPhysicalEquipmentBO
     */
    public InPhysicalEquipmentRest getByDigitalServiceVersionAndId(final String digitalServiceVersionUid, Long id) {
        final Optional<InPhysicalEquipment> inPhysicalEquipment = inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id);
        if (inPhysicalEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no physical equipment with id : %s", digitalServiceVersionUid, id));
        }

        if (!Objects.equals(digitalServiceVersionUid, inPhysicalEquipment.get().getDigitalServiceVersionUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this physical equipment id: %d", digitalServiceVersionUid, inPhysicalEquipment.get().getDigitalServiceVersionUid(), id));
        }

        return inPhysicalEquipmentMapper.toRest(inPhysicalEquipment.get());
    }

    /**
     * Create a new in physical equipment for a specific digital service.
     *
     * @param digitalServiceVersionUid      the digitalServiceVersionUid.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on physical equipment created.
     */
    public InPhysicalEquipmentRest createInPhysicalEquipmentDigitalServiceVersion(final String digitalServiceVersionUid, final InPhysicalEquipmentRest inVirtualEquipmentRest) {
        Optional<DigitalServiceVersion> digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid);

        if (digitalServiceVersion.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service version of uid : %s, doesn't exist", digitalServiceVersionUid));
        }

        final InPhysicalEquipment inVirtualEquipmentToCreate = inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentRest);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceVersion.get().getDigitalService().getUid());
        inVirtualEquipmentToCreate.setDigitalServiceVersionUid(digitalServiceVersionUid);
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inPhysicalEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inPhysicalEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a physical equipment.
     *
     * @param digitalServiceVersionUid            the digitalServiceUid.
     * @param id                           the physical equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InPhysicalEquipmentRest updateInPhysicalEquipment(final String digitalServiceVersionUid, final Long id, final InPhysicalEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InPhysicalEquipment> inVirtualEquipment = inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no physical equipment with id : %s", digitalServiceVersionUid, id));
        }

        if (!Objects.equals(digitalServiceVersionUid, inVirtualEquipment.get().getDigitalServiceUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this physical equipment id: %d", digitalServiceVersionUid, inVirtualEquipment.get().getDigitalServiceVersionUid(), id));
        }

        final InPhysicalEquipment objectToUpdate = inVirtualEquipment.get();
        final InPhysicalEquipment updates = inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inPhysicalEquipmentMapper.merge(objectToUpdate, updates);

        inPhysicalEquipmentRepository.save(objectToUpdate);
        return inPhysicalEquipmentMapper.toRest(objectToUpdate);
    }

    // *** INVENTORY PART ***

    /**
     * Get the physical equipments list linked to an inventory
     *
     * @param inventoryId the inventory id
     * @return the physical equipment list.
     */
    public List<InPhysicalEquipmentRest> getByInventory(final Long inventoryId) {
        final List<InPhysicalEquipment> inVirtualEquipment = inPhysicalEquipmentRepository.findByInventoryId(inventoryId);
        return inPhysicalEquipmentMapper.toRest(inVirtualEquipment);
    }

    /**
     * Retrieving a physical equipment for an inventory and a physical equipment id.
     *
     * @param inventoryId the inventory id
     * @param id          the physical equipment id
     * @return InPhysicalEquipmentBO
     */
    public InPhysicalEquipmentRest getByInventoryAndId(final Long inventoryId, Long id) {
        final Optional<InPhysicalEquipment> inVirtualEquipment = inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no physical equipment with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inVirtualEquipment.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this physical equipment id: %d", inventoryId, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        return inPhysicalEquipmentMapper.toRest(inVirtualEquipment.get());
    }

    /**
     * Create a new in physical equipment for a specific inventory.
     *
     * @param inventoryId            the inventory id.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on physical equipment created.
     */
    public InPhysicalEquipmentRest createInPhysicalEquipmentInventory(final Long inventoryId, final InPhysicalEquipmentRest inVirtualEquipmentRest) {
        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);

        if (inventory.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory of id : %s, doesn't exist", inventoryId));
        }

        final InPhysicalEquipment inVirtualEquipmentToCreate = inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentRest);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setInventoryId(inventoryId);
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inPhysicalEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inPhysicalEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a physical equipment.
     *
     * @param inventoryId                  the inventory id.
     * @param id                           the physical equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InPhysicalEquipmentRest updateInPhysicalEquipment(final Long inventoryId, final Long id, final InPhysicalEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InPhysicalEquipment> inVirtualEquipment = inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no physical equipment with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inVirtualEquipment.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this physical equipment id: %d", inventoryId, inVirtualEquipment.get().getInventoryId(), id));
        }

        final InPhysicalEquipment objectToUpdate = inVirtualEquipment.get();
        final InPhysicalEquipment updates = inPhysicalEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inPhysicalEquipmentMapper.merge(objectToUpdate, updates);

        inPhysicalEquipmentRepository.save(objectToUpdate);
        return inPhysicalEquipmentMapper.toRest(objectToUpdate);
    }

    /**
     * Delete the physical equipment of a digital service
     *
     * @param digitalServiceVersionUid the digital service uid
     * @param id                the physical equipment id.
     */
    public void deleteInPhysicalEquipment(final String digitalServiceVersionUid, final Long id) {
        inPhysicalEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id)
                .orElseThrow(() -> new G4itRestException("404", String.format("Physical equipment %d not found in digital service %s", id, digitalServiceVersionUid)));
        inPhysicalEquipmentRepository.deleteById(id);
    }

    /**
     * Delete the physical equipment of an inventory
     *
     * @param inventoryId the inventory id
     * @param id          the physical equipment id.
     */
    public void deleteInPhysicalEquipment(final Long inventoryId, final Long id) {
        inPhysicalEquipmentRepository.findByInventoryIdAndId(inventoryId, id)
                .orElseThrow(() -> new G4itRestException("404", String.format("Physical equipment %d not found in inventory %d", id, inventoryId)));
        inPhysicalEquipmentRepository.deleteById(id);
    }

}

