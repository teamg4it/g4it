/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.VirtualEquipment;
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
 * Virtual Equipement JPA repository.
 */
@Repository
public interface VirtualEquipmentRepository extends
        AbstractValidationBaseEntityRepository<VirtualEquipment>,
        JpaRepository<VirtualEquipment, Long> {

    String WHERE_PHYSICAL_EQUIPMENT_INDICATED_BUT_NOT_EXIST = "where ve.sessionDate = :sessionDate " +
            "and not exists (select ep from ve.physicalEquipment ep where ep.nomEquipementPhysique = ve.nomEquipementPhysique)";

    /**
     * Find virtual equipment by functionally unique id
     *
     * @param inventoryId          inventory Id
     * @param nomEquipementVirtuel VM name
     */
    Optional<VirtualEquipment> findByInventoryIdAndNomEquipementVirtuel(final long inventoryId,
                                                                        final String nomEquipementVirtuel);

    /**
     * Find virtual equipment not linked to a Physical Equipment.
     *
     * @param sessionDate the session date.
     * @param pageable    pagination.
     * @return virtual equipments not linked to a Physical Equipment.
     */
    @Query(value = "select ve from VirtualEquipment ve "
            + WHERE_PHYSICAL_EQUIPMENT_INDICATED_BUT_NOT_EXIST)
    Page<VirtualEquipment> findVirtualEquipmentNotLinkedToPhysicalEquipment(final Date sessionDate,
                                                                            final Pageable pageable);

    /**
     * Delete virtual equipments not linked to inventory or physical equipment.
     *
     * @param sessionDate the session date.
     */
    @Modifying
    @Query(value = "delete from VirtualEquipment ve "
            + WHERE_PHYSICAL_EQUIPMENT_INDICATED_BUT_NOT_EXIST)
    void deleteAfterConsistencyControl(final Date sessionDate);

    List<VirtualEquipment> findByInventoryId(long inventoryId);

}
