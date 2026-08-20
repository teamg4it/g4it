/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceVersionRepository;
import com.soprasteria.g4it.backend.apiinout.mapper.InVirtualEquipmentMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.common.utils.CommonValidationUtil;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class InVirtualEquipmentService {

    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    private InVirtualEquipmentMapper inVirtualEquipmentMapper;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private InventoryRepository inventoryRepository;
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    private CommonValidationUtil commonValidationUtil;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get the virtual equipments list linked to a digital service.
     *
     * @param digitalServiceVersionUid the digital service UID.
     * @return the virtual equipment list.
     */
    /*public List<InVirtualEquipmentRest> getByDigitalServiceVersion(final String digitalServiceVersionUid) {
        final List<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceVersionUidOrderByName(digitalServiceVersionUid);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipment);
    }*/
    @Transactional(readOnly = true)
    public List<InVirtualEquipmentRest> getByDigitalServiceVersion(
            final String digitalServiceVersionUid) {

        List<InVirtualEquipmentRest> result = new ArrayList<>();

        int pageNumber = 0;

        while(true){
            Pageable page = PageRequest.of(
                    pageNumber,
                    Constants.BATCH_SIZE_50000
            );

            List<InVirtualEquipment> inVirtualEquipments = inVirtualEquipmentRepository
                    .findByDigitalServiceVersionUidOrderByNameAscIdAsc(
                            digitalServiceVersionUid, page);
            if(inVirtualEquipments.isEmpty()){
                break;
            }
            result.addAll(inVirtualEquipmentMapper.toRest(inVirtualEquipments));
            inVirtualEquipments.clear();
            entityManager.clear();
            pageNumber++;
        }
        return result;
    }

    /**
     * Retrieving a virtual equipment for a digital service and a virtual equipment id.
     *
     * @param digitalServiceVersionUid the digital service UID.
     * @param id                the virtual equipment id
     * @return InVirtualEquipmentBO
     */
    public InVirtualEquipmentRest getByDigitalServiceVersionAndId(final String digitalServiceVersionUid, Long id) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no virtual equipment with id : %s", digitalServiceVersionUid, id));
        }

        if (!Objects.equals(digitalServiceVersionUid, inVirtualEquipment.get().getDigitalServiceVersionUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this virtual equipment id: %d", digitalServiceVersionUid, inVirtualEquipment.get().getDigitalServiceVersionUid(), id));
        }

        return inVirtualEquipmentMapper.toRest(inVirtualEquipment.get());
    }

    /**
     * Create a new in virtual equipment for a specific digital service.
     *
     * @param digitalServiceVersionUid      the digitalServiceUid.
     * @param inVirtualEquipmentRest the inVirtualEquipmentRest.
     * @return the business object corresponding on virtual equipment created.
     */
    public InVirtualEquipmentRest createInVirtualEquipmentDigitalServiceVersion(final String digitalServiceVersionUid, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        Optional<DigitalServiceVersion> digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid);

        if (digitalServiceVersion.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service of uid : %s, doesn't exist", digitalServiceVersionUid));
        }

        validateLocationByBoaviztaApi(inVirtualEquipmentRest);
        final InVirtualEquipment inVirtualEquipmentToCreate = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentRest);
        inVirtualEquipmentToCreate.setId(null);
        final LocalDateTime now = LocalDateTime.now();
        inVirtualEquipmentToCreate.setDigitalServiceVersionUid(digitalServiceVersionUid);
        inVirtualEquipmentToCreate.setDigitalServiceUid(digitalServiceVersion.get().getDigitalService().getUid());
        inVirtualEquipmentToCreate.setCreationDate(now);
        inVirtualEquipmentToCreate.setLastUpdateDate(now);

        inVirtualEquipmentRepository.save(inVirtualEquipmentToCreate);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipmentToCreate);
    }

    /**
     * Update a virtual equipment.
     *
     * @param digitalServiceVersionUid            the digitalServiceUid.
     * @param id                           the virtual equipment's id
     * @param inVirtualEquipmentUpdateRest the inVirtualEquipmentUpdateRest.
     * @return InventoryBO
     */
    public InVirtualEquipmentRest updateInVirtualEquipment(final String digitalServiceVersionUid, final Long id, final InVirtualEquipmentRest inVirtualEquipmentUpdateRest) {
        final Optional<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id);
        if (inVirtualEquipment.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service uid provided: %s has no virtual equipment with id : %s", digitalServiceVersionUid, id));
        }

        if (!Objects.equals(digitalServiceVersionUid, inVirtualEquipment.get().getDigitalServiceVersionUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this virtual equipment id: %d", digitalServiceVersionUid, inVirtualEquipment.get().getDigitalServiceVersionUid(), id));
        }

        validateLocationByBoaviztaApi(inVirtualEquipmentUpdateRest);
        final InVirtualEquipment objectToUpdate = inVirtualEquipment.get();
        final InVirtualEquipment updates = inVirtualEquipmentMapper.toEntity(inVirtualEquipmentUpdateRest);
        inVirtualEquipmentMapper.merge(objectToUpdate, updates);

        inVirtualEquipmentRepository.save(objectToUpdate);
        return inVirtualEquipmentMapper.toRest(objectToUpdate);
    }

    public List<InVirtualEquipmentRest> updateOrDeleteInVirtualEquipments(final String digitalServiceVersionUid,
                                                                          final Long physicalEqpId,
                                                                          final List<InVirtualEquipmentRest> inVirtualEquipmentList) {

        List<InVirtualEquipmentRest> updatedEquipments = new ArrayList<>();
        InPhysicalEquipment physicalEqpEntity = inPhysicalEquipmentRepository.findById(physicalEqpId)
                .orElseThrow(() -> new G4itRestException("404", String.format(
                        "The digitalService id provided: %s has no physical equipment with id: %s",
                        digitalServiceVersionUid, physicalEqpId
                )));
        String physicalEqpName = physicalEqpEntity.getName();

        // All the vms related to a server are deleted
        if (inVirtualEquipmentList.isEmpty()) {
            List<InVirtualEquipment> virtualEqpToDelete = inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndPhysicalEquipmentName(digitalServiceVersionUid, physicalEqpName);
            if (!virtualEqpToDelete.isEmpty()) {
                inVirtualEquipmentRepository.deleteAll(virtualEqpToDelete);
            }
            return updatedEquipments;
        }

        // Get existing equipment from repository
        List<InVirtualEquipment> existingEquipments = inVirtualEquipmentRepository
                .findByDigitalServiceVersionUidAndPhysicalEquipmentName(
                        digitalServiceVersionUid,
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
                    digitalServiceVersionUid,
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
    /*public List<InVirtualEquipmentRest> getByInventory(final Long inventoryId) {
        final List<InVirtualEquipment> inVirtualEquipment = inVirtualEquipmentRepository.findByInventoryId(inventoryId);
        return inVirtualEquipmentMapper.toRest(inVirtualEquipment);
    }*/
    @Transactional(readOnly = true)
    public List<InVirtualEquipmentRest> getByInventory(final Long inventoryId) {

        List<InVirtualEquipmentRest> result = new ArrayList<>();
        int pageNumber = 0;

        while(true){
            Pageable page = PageRequest.of(
                    pageNumber,
                    Constants.BATCH_SIZE_50000
            );

            List<InVirtualEquipment> inVirtualEquipments = inVirtualEquipmentRepository
                    .findByInventoryIdOrderByIdAsc(inventoryId, page);
            if(inVirtualEquipments.isEmpty()){
                break;
            }
            result.addAll(inVirtualEquipmentMapper.toRest(inVirtualEquipments));
            log.info(
                    "Processed in_virtual_equipment page={}, records={}, totalResult={}",
                    pageNumber,
                    inVirtualEquipments.size(),
                    result.size()
            );
            inVirtualEquipments.clear();
            entityManager.clear();
            pageNumber++;
        }
        return result;
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
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this virtual equipment id: %d", inventoryId, inVirtualEquipment.get().getDigitalServiceVersionUid(), id));
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
     * @param digitalServiceVersionUid the digital service uid
     * @param id                the virtual equipment id.
     */
    public void deleteInVirtualEquipment(final String digitalServiceVersionUid, final Long id) {
        inVirtualEquipmentRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id)
                .orElseThrow(() -> new G4itRestException("404", String.format("Virtual equipment %d not found in digital service %s", id, digitalServiceVersionUid)));
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

    private void validateLocationByBoaviztaApi(InVirtualEquipmentRest inVirtualEquipmentUpdateRest) {
        if (inVirtualEquipmentUpdateRest.getLocation() != null && !inVirtualEquipmentUpdateRest.getLocation().isBlank()) {
            if (!commonValidationUtil.validateboaviztaCountry(inVirtualEquipmentUpdateRest.getLocation())) {
                throw new G4itRestException("400", String.format("Selected Country : %s, doesn't exist", inVirtualEquipmentUpdateRest.getLocation()));
            }
        }
    }

}

