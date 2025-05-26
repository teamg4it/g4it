/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class InVirtualEquipmentService {

    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;
    private DigitalServiceRepository digitalServiceRepository;
    private InventoryRepository inventoryRepository;
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;

    /**
     * Get the virtual equipments list linked to a digital service.
     *
     * @param digitalServiceUid the digital service UID.
     * @return the virtual equipment list.
     */
    public List<InVirtualEquipmentRest> getByDigitalService(final String digitalServiceUid) {
        final List<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceUidOrderByName(digitalServiceUid);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipment);
    }

    /**
     * Retrieving a virtual equipment for a digital service and a virtual equipment id.
     *
     * @param digitalServiceUid the digital service UID.
     * @param id                the virtual equipment id
     * @return InVirtualEquipmentBO
     */
    public InVirtualEquipmentRest getByDigitalServiceAndId(final String digitalServiceUid, Long id) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no virtual equipment with id : %s", digitalServiceUid, id));
        }

        if (!Objects.equals(digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this virtual equipment id: %d", digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        return inVirtualEquipmentMapper.toRest(inVirtualEquipment.get());
    }

    /**
     * Create a new in virtual equipment for a specific digital service.
     *
     * @param digitalServiceUid      the digitalServiceUid.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on virtual equipment created.
     */
    public InVirtualEquipmentRest createInVirtualEquipmentDigitalService(final String digitalServiceUid, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        Optional<DigitalService> digitalService = digitalServiceRepository.findById(digitalServiceUid);

        if (digitalService.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceUid));
        }

        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest);
        inVirtualEquipmentToCreate.setId(null);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceUid);
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a virtual equipment.
     *
     * @param digitalServiceUid            the digitalServiceUid.
     * @param id                           the virtual equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InVirtualEquipmentRest updateInVirtualEquipment(final String digitalServiceUid, final Long id, final InVirtualEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no virtual equipment with id : %s", digitalServiceUid, id));
        }

        if (!Objects.equals(digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this virtual equipment id: %d", digitalServiceUid, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        final InVirtualEquipment objectToUpdate = inVirtualEquipment.get();
        final InVirtualEquipment updates = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inVirtualEquipmentMapper.merge(objectToUpdate, updates);

        inVirtualEquipmentRepository.save(objectToUpdate);
        return inVirtualEquipmentMapper.toRest(objectToUpdate);
    }

    public List<InVirtualEquipmentRest> updateOrDeleteInVirtualEquipments(final String digitalServiceUid,
                                                                          final Long physicalEqpId,
                                                                          final List<InVirtualEquipmentRest> inVirtualEquipmentList) {

        List<InVirtualEquipmentRest> updatedEquipments = new ArrayList<>();
        InPhysicalEquipment physicalEqpEntity = inPhysicalEquipmentRepository.findById(physicalEqpId)
                .orElseThrow(() -> new G4itRestException("404", String.format(
                        "The digitalService id provided: %s has no physical equipment with id: %s",
                        digitalServiceUid, physicalEqpId
                )));
        String physicalEqpName = physicalEqpEntity.getName();

        // All the vms related to a server are deleted
        if (inVirtualEquipmentList.isEmpty()) {
            List<InVirtualEquipment> virtualEqpToDelete = inVirtualEquipmentRepository.findByDigitalServiceUidAndPhysicalEquipmentName(digitalServiceUid, physicalEqpName);
            if (!virtualEqpToDelete.isEmpty()) {
                inVirtualEquipmentRepository.deleteAll(virtualEqpToDelete);
            }
            return updatedEquipments;
        }

        // Get existing equipment from repository
        List<InVirtualEquipment> existingEquipments = inVirtualEquipmentRepository
                .findByDigitalServiceUidAndPhysicalEquipmentName(
                        digitalServiceUid,
                        physicalEqpName
                );

        // Get list of IDs from input list
        List<Long> inputEquipmentIds = inVirtualEquipmentList.stream()
                .map(InVirtualEquipmentRest::getId)
                .toList();

        // Find and delete equipment that exists in repository but not in input list
        List<InVirtualEquipment> equipmentsToDelete = existingEquipments.stream()
                .filter(equipment -> !inputEquipmentIds.contains(equipment.getId()))
                .toList();

        // Delete equipment that exists in repository but not in input list
        if (!equipmentsToDelete.isEmpty()) {
            inVirtualEquipmentRepository.deleteAll(equipmentsToDelete);
        }

        // Updates the other equipments
        for (InVirtualEquipmentRest inVirtualEquipment : inVirtualEquipmentList) {
            InVirtualEquipmentRest inVirtualEquipmentRest = updateInVirtualEquipment(
                    digitalServiceUid,
                    inVirtualEquipment.getId(),
                    inVirtualEquipment
            );
            updatedEquipments.add(inVirtualEquipmentRest);
        }
        return updatedEquipments;
    }


    // *** INVENTORY PART ***

    /**
     * Get the virtual equipments list linked to an inventory
     *
     * @param inventoryId the inventory id
     * @return the virtual equipment list.
     */
    public List<InVirtualEquipmentRest> getByInventory(final Long inventoryId) {
        final List<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryId(inventoryId);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipment);
    }

    /**
     * Retrieving a virtual equipment for an inventory and a virtual equipment id.
     *
     * @param inventoryId the inventory id
     * @param id          the virtual equipment id
     * @return InVirtualEquipmentBO
     */
    public InVirtualEquipmentRest getByInventoryAndId(final Long inventoryId, Long id) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no virtual equipment with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inVirtualEquipment.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this virtual equipment id: %d", inventoryId, inVirtualEquipment.get().getDigitalServiceUid(), id));
        }

        return inVirtualEquipmentMapper.toRest(inVirtualEquipment.get());
    }

    /**
     * Create a new in virtual equipment for a specific inventory.
     *
     * @param inventoryId            the inventory id.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on virtual equipment created.
     */
    public InVirtualEquipmentRest createInVirtualEquipmentInventory(final Long inventoryId, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);

        if (inventory.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory of id : %s, doesn't exist", inventoryId));
        }

        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setInventoryId(inventoryId);
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a virtual equipment.
     *
     * @param inventoryId                  the inventory id.
     * @param id                           the virtual equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InVirtualEquipmentRest updateInVirtualEquipment(final Long inventoryId, final Long id, final InVirtualEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no virtual equipment with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inVirtualEquipment.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this virtual equipment id: %d", inventoryId, inVirtualEquipment.get().getInventoryId(), id));
        }

        final InVirtualEquipment objectToUpdate = inVirtualEquipment.get();
        final InVirtualEquipment updates = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inVirtualEquipmentMapper.merge(objectToUpdate, updates);

        inVirtualEquipmentRepository.save(objectToUpdate);
        return inVirtualEquipmentMapper.toRest(objectToUpdate);
    }

    /**
     * Delete the virtual equipment of a digital service
     *
     * @param digitalServiceUid the digital service uid
     * @param id                the virtual equipment id.
     */
    public void deleteInVirtualEquipment(final String digitalServiceUid, final Long id) {
        inVirtualEquipmentRepository.findByDigitalServiceUidAndId(digitalServiceUid, id)
                .orElseThrow(() -> new G4itRestException("404", String.format("Virtual equipment %d not found in digital service %s", id, digitalServiceUid)));
        inVirtualEquipmentRepository.deleteById(id);
    }

    /**
     * Delete the virtual equipment of an inventory
     *
     * @param inventoryId the inventory id
     * @param id          the virtual equipment id.
     */
    public void deleteInVirtualEquipment(final Long inventoryId, final Long id) {
        inVirtualEquipmentRepository.findByInventoryIdAndId(inventoryId, id)
                .orElseThrow(() -> new G4itRestException("404", String.format("Virtual equipment %d not found in inventory %d", id, inventoryId)));
        inVirtualEquipmentRepository.deleteById(id);
    }

}

