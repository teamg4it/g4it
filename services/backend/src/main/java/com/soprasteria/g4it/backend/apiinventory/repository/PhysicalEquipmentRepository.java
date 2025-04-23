/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Physical Equipement JPA repository.
 */
@Repository
public interface PhysicalEquipmentRepository extends
        AbstractValidationBaseEntityRepository<PhysicalEquipment>,
        JpaRepository<PhysicalEquipment, Long> {

    String WHERE_DATACENTER_INDICATED_BUT_NOT_EXIST =
            "where ep.sessionDate=:sessionDate and ep.nomCourtDatacenter is not null " +
                    "and ep.nomCourtDatacenter <> '' " +
                    "and not exists(" +
                    "   select dc from ep.datacenter dc " +
                    "   where dc.inventoryId = ep.inventoryId " +
                    "   and dc.nomCourtDatacenter = ep.nomCourtDatacenter" +
                    ")";

    /**
     * Find physical equipment by the functionally unique fields
     *
     * @param inventoryId           inventory Identifier
     * @param nomEquipementPhysique physical equipment name
     * @return return a list of physical equipment
     */
    Optional<PhysicalEquipment> findByInventoryIdAndNomEquipementPhysique(long inventoryId,
                                                                          String nomEquipementPhysique);

    /**
     * Find physical equipment not linked to a DataCenter.
     *
     * @param sessionDate the session date.
     * @param pageable    pagination.
     * @return physical equipments not linked to a DataCenter.
     */
    @Query(value = "select ep from PhysicalEquipment ep " + WHERE_DATACENTER_INDICATED_BUT_NOT_EXIST)
    Page<PhysicalEquipment> findPhysicalEquipmentNotLinkedToDataCenter(final Date sessionDate,
                                                                       final Pageable pageable);

    /**
     * Delete physical equipments not linked to inventory or DataCenter.
     *
     * @param sessionDate the session date.
     */
    @Modifying
    @Query(value = "delete from PhysicalEquipment ep " + WHERE_DATACENTER_INDICATED_BUT_NOT_EXIST)
    void deleteAfterConsistencyControl(final Date sessionDate);

    /**
     * Count physical equipments linked to an inventory
     *
     * @param inventoryId inventory Id
     */
    @Query(value = "select coalesce(sum(cast(quantite as numeric)),0) from equipement_physique ep where ep.inventory_id = :inventoryId", nativeQuery = true)
    long countByInventoryId(long inventoryId);

    List<PhysicalEquipment> findByInventoryId(Long inventoryId, Pageable pageable);
}
