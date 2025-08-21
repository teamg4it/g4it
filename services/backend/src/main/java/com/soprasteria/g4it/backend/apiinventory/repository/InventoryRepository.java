/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository to access Inventory Data in database.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Delete inventory and return number of lines deleted
     *
     * @param inventoryId the unique inventory identifier
     */
    @Transactional
    @Modifying
    @Query("delete from Inventory i where i.id=:inventoryId")
    void deleteByInventoryId(@Param("inventoryId") final Long inventoryId);

    /**
     * Find by organization and the inventory id and return  a list of matching inventories
     *
     * @param workspace the unique organization identifier.
     * @return a list of matching inventories
     */
    Optional<Inventory> findByWorkspaceAndId(final Workspace workspace,
                                             final Long inventoryId);

    /**
     * Find by subscriber and organization then return a list of inventories
     *
     * @param workspace the linked organization
     * @return a list of inventory
     */
    List<Inventory> findByWorkspace(final Workspace workspace);


    /**
     * Find by organization and the inventory name and return  a list of matching inventories
     *
     * @param workspace the unique organization identifier.
     * @param name      the inventory name.
     * @return a list of matching inventories
     */
    Optional<Inventory> findByWorkspaceAndName(final Workspace workspace, final String name);

}
