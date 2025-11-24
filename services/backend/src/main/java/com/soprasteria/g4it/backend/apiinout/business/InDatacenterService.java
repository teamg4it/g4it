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
import com.soprasteria.g4it.backend.apiinout.mapper.InDatacenterMapper;
import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
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
public class InDatacenterService {

    private InDatacenterRepository inDatacenterRepository;
    private InDatacenterMapper inDatacenterMapper;
    private DigitalServiceVersionRepository digitalServiceVersionRepository;
    private InventoryRepository inventoryRepository;

    /**
     * Get the datacenters list linked to a digital service.
     *
     * @param digitalServiceVersionUid the digital service UID.
     * @return the datacenter list.
     */
    public List<InDatacenterRest> getByDigitalServiceVersion(final String digitalServiceVersionUid) {
        final List<InDatacenter> inDatacenters = inDatacenterRepository.findByDigitalServiceVersionUid(digitalServiceVersionUid);
        return inDatacenterMapper.toRest(inDatacenters);
    }

    /**
     * Retrieving a datacenter for a digital service and a datacenter id.
     *
     * @param digitalServiceVersionUid the digital service UID.
     * @param id                the datacenter id
     * @return InDatacenterBO
     */
    public InDatacenterRest getByDigitalServiceVersionAndId(final String digitalServiceVersionUid, Long id) {
        final Optional<InDatacenter> inDatacenter = inDatacenterRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id);
        if (inDatacenter.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service version uid provided: %s has no datacenter with id : %s", digitalServiceVersionUid, id));
        }

        if (!Objects.equals(digitalServiceVersionUid, inDatacenter.get().getDigitalServiceVersionUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this datacenter id: %d", digitalServiceVersionUid, inDatacenter.get().getDigitalServiceUid(), id));
        }

        return inDatacenterMapper.toRest(inDatacenter.get());
    }

    /**
     * Create a new in datacenter for a specific digital service.
     *
     * @param digitalServiceVersionUid the digitalServiceUid.
     * @param inDatacenterRest  the inDatacenterRest.
     * @return the business object corresponding on datacenter created.
     */
    public InDatacenterRest createInDatacenterDigitalServiceVersion(final String digitalServiceVersionUid, final InDatacenterRest inDatacenterRest) {
        Optional<DigitalServiceVersion> digitalServiceVersion = digitalServiceVersionRepository.findById(digitalServiceVersionUid);

        if (digitalServiceVersion.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service version of uid : %s, doesn't exist", digitalServiceVersionUid));
        }

        final InDatacenter inDatacenterToCreate = inDatacenterMapper.toEntity(inDatacenterRest);
        final LocalDateTime now = LocalDateTime.now();
        inDatacenterToCreate.setDigitalServiceUid(digitalServiceVersion.get().getDigitalService().getUid());
        inDatacenterToCreate.setDigitalServiceVersionUid(digitalServiceVersionUid);
        inDatacenterToCreate.setCreationDate(now);
        inDatacenterToCreate.setLastUpdateDate(now);

        inDatacenterRepository.save(inDatacenterToCreate);
        return inDatacenterMapper.toRest(inDatacenterToCreate);
    }

    /**
     * Update a datacenter.
     *
     * @param digitalServiceVersionUid      the digitalServiceUid.
     * @param id                     the datacenter's id
     * @param inDatacenterUpdateRest the inDatacenterUpdateRest.
     * @return InventoryBO
     */
    public InDatacenterRest updateInDatacenter(final String digitalServiceVersionUid, final Long id, final InDatacenterRest inDatacenterUpdateRest) {
        final Optional<InDatacenter> inDatacenter = inDatacenterRepository.findByDigitalServiceVersionUidAndId(digitalServiceVersionUid, id);
        if (inDatacenter.isEmpty()) {
            throw new G4itRestException("404", String.format("the digital service version uid provided: %s has no datacenter with id : %s", digitalServiceVersionUid, id));
        }

        if (!Objects.equals(digitalServiceVersionUid, inDatacenter.get().getDigitalServiceVersionUid())) {
            throw new G4itRestException("409", String.format("the digital service uid provided: %s is not compatible with the digital uid : %s linked to this datacenter id: %d", digitalServiceVersionUid, inDatacenter.get().getDigitalServiceUid(), id));
        }

        final InDatacenter objectToUpdate = inDatacenter.get();
        final InDatacenter updates = inDatacenterMapper.toEntity(inDatacenterUpdateRest);
        inDatacenterMapper.merge(objectToUpdate, updates);

        inDatacenterRepository.save(objectToUpdate);
        return inDatacenterMapper.toRest(objectToUpdate);
    }

    // *** INVENTORY PART ***

    /**
     * Get the datacenters list linked to an inventory
     *
     * @param inventoryId the inventory id
     * @return the datacenter list.
     */
    public List<InDatacenterRest> getByInventory(final Long inventoryId) {
        final List<InDatacenter> inDatacenter = inDatacenterRepository.findByInventoryId(inventoryId);
        return inDatacenterMapper.toRest(inDatacenter);
    }

    /**
     * Retrieving a datacenter for an inventory and a datacenter id.
     *
     * @param inventoryId the inventory id
     * @param id          the datacenter id
     * @return InDatacenterBO
     */
    public InDatacenterRest getByInventoryAndId(final Long inventoryId, Long id) {
        final Optional<InDatacenter> inDatacenter = inDatacenterRepository.findByInventoryIdAndId(inventoryId, id);
        if (inDatacenter.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no datacenter with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inDatacenter.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this datacenter id: %d", inventoryId, inDatacenter.get().getDigitalServiceUid(), id));
        }

        return inDatacenterMapper.toRest(inDatacenter.get());
    }

    /**
     * Create a new in datacenter for a specific inventory.
     *
     * @param inventoryId      the inventory id.
     * @param inDatacenterRest the inDatacenterRest.
     * @return the business object corresponding on datacenter created.
     */
    public InDatacenterRest createInDatacenterInventory(final Long inventoryId, final InDatacenterRest inDatacenterRest) {
        Optional<Inventory> inventory = inventoryRepository.findById(inventoryId);

        if (inventory.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory of id : %s, doesn't exist", inventoryId));
        }

        final InDatacenter inDatacenterToCreate = inDatacenterMapper.toEntity(inDatacenterRest);
        final LocalDateTime now = LocalDateTime.now();
        inDatacenterToCreate.setInventoryId(inventoryId);
        inDatacenterToCreate.setCreationDate(now);
        inDatacenterToCreate.setLastUpdateDate(now);

        inDatacenterRepository.save(inDatacenterToCreate);
        return inDatacenterMapper.toRest(inDatacenterToCreate);
    }

    /**
     * Update a datacenter.
     *
     * @param inventoryId            the inventory id.
     * @param id                     the datacenter's id
     * @param inDatacenterUpdateRest the inDatacenterUpdateRest.
     * @return InventoryBO
     */
    public InDatacenterRest updateInDatacenter(final Long inventoryId, final Long id, final InDatacenterRest inDatacenterUpdateRest) {
        final Optional<InDatacenter> inDatacenter = inDatacenterRepository.findByInventoryIdAndId(inventoryId, id);
        if (inDatacenter.isEmpty()) {
            throw new G4itRestException("404", String.format("the inventory id provided: %s has no datacenter with id : %s", inventoryId, id));
        }

        if (!Objects.equals(inventoryId, inDatacenter.get().getInventoryId())) {
            throw new G4itRestException("409", String.format("the inventory id provided: %s is not compatible with the inventory id : %s linked to this datacenter id: %d", inventoryId, inDatacenter.get().getInventoryId(), id));
        }

        final InDatacenter objectToUpdate = inDatacenter.get();
        final InDatacenter updates = inDatacenterMapper.toEntity(inDatacenterUpdateRest);
        inDatacenterMapper.merge(objectToUpdate, updates);

        inDatacenterRepository.save(objectToUpdate);
        return inDatacenterMapper.toRest(objectToUpdate);
    }

    /**
     * Delete a datacenter
     *
     * @param id the datacenter id.
     */
    public void deleteInDatacenter(final Long id) {
        inDatacenterRepository.deleteById(id);
    }

}

