/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InPhysicalEquipment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * In Physical Equipment JPA repository.
 */
@Repository
public interface InPhysicalEquipmentRepository extends JpaRepository<InPhysicalEquipment, Long> {

    /**
     * Find physical equipment by the functionally unique fields
     *
     * @param digitalServiceUid digital service Identifier
     * @param id                physical equipment id
     * @return return a physical equipment
     */
    Optional<InPhysicalEquipment> findByDigitalServiceUidAndId(String digitalServiceUid, Long id);

    /**
     * Find physical equipments of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of physical equipments
     */
    List<InPhysicalEquipment> findByDigitalServiceUid(String digitalServiceUid);

    List<InPhysicalEquipment> findByDigitalServiceUid(String digitalServiceUid, Pageable pageable);

    /**
     * Find physical equipment by the functionally unique fields
     *
     * @param inventoryId inventory id
     * @param id          physical equipment id
     * @return return a physical equipment
     */
    Optional<InPhysicalEquipment> findByInventoryIdAndId(Long inventoryId, Long id);

    /**
     * Find physical equipments of one digital service
     *
     * @param inventoryId inventory id
     * @return return a list of physical equipments
     */
    List<InPhysicalEquipment> findByInventoryId(Long inventoryId);

    List<InPhysicalEquipment> findByInventoryId(Long inventoryId, Pageable pageable);

    long countByInventoryId(Long inventoryId);

    long countByDigitalServiceUid(String digitalServiceUid);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndNameIn(Long inventoryId, Set<String> names);

    /**
     * Count physical equipments linked to an inventory
     *
     * @param inventoryId inventory Id
     * @return the sum of quantity
     */
    @Query("select coalesce(sum(quantity), 0) from InPhysicalEquipment ep where ep.inventoryId = :inventoryId")
    Long sumQuantityByInventoryId(Long inventoryId);

    @Transactional
    @Modifying
    void deleteByInventoryId(Long inventoryId);

    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);
}
