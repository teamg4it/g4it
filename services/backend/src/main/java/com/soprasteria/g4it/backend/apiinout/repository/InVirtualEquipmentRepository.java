/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InVirtualEquipment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * In Virtual Equipment JPA repository.
 */
@Repository
public interface InVirtualEquipmentRepository extends JpaRepository<InVirtualEquipment, Long> {

    /**
     * Find virtual equipment by the functionally unique fields
     *
     * @param digitalServiceUid digital service Identifier
     * @param id                virtual equipment id
     * @return return a virtual equipment
     */
    Optional<InVirtualEquipment> findByDigitalServiceUidAndId(String digitalServiceUid, Long id);

    /**
     * Find virtual equipments of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceUid(String digitalServiceUid);

    /**
     * Find virtual equipment by the functionally unique fields
     *
     * @param inventoryId inventory id
     * @param id          virtual equipment id
     * @return return a virtual equipment
     */
    Optional<InVirtualEquipment> findByInventoryIdAndId(Long inventoryId, Long id);

    /**
     * Find virtual equipments of one inventory
     *
     * @param inventoryId inventory id
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByInventoryId(Long inventoryId);

    /**
     * Find virtual equipments of one inventory and one physical equipment name
     *
     * @param inventoryId           inventory i
     * @param physicalEquipmentName physicalEquipmentName
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByInventoryIdAndPhysicalEquipmentName(Long inventoryId, String physicalEquipmentName, Pageable pageable);

    /**
     * Find virtual equipments of one inventory and one physical equipment name
     *
     * @param digitalServiceUid     digitalServiceUid
     * @param physicalEquipmentName physicalEquipmentName
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceUidAndPhysicalEquipmentName(String digitalServiceUid, String physicalEquipmentName, Pageable pageable);

    /**
     * Find virtual equipments of one inventory and one physical equipment name
     *
     * @param digitalServiceUid     digitalServiceUid
     * @param physicalEquipmentName physicalEquipmentName
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceUidAndPhysicalEquipmentName(String digitalServiceUid, String physicalEquipmentName);

    /**
     * Count virtual equipments linked to an inventory
     *
     * @param inventoryId inventory Id
     * @return the sum of quantity
     */
    @Query("select coalesce(sum(quantity), 0) from InVirtualEquipment ev where ev.inventoryId = :inventoryId")
    Long sumQuantityByInventoryId(Long inventoryId);

    /**
     * Count distinct virtual equipments linked to an inventory
     *
     * @param inventoryId the unique inventory identifier.
     * @return the sum of quantity of distinct virtual equipments.
     */
    @Query("""
            select coalesce(sum(quantity), 0)
            from (
                select
                    ive.name as name,
                    ive.quantity as quantity
                from
                    InVirtualEquipment ive
                where ive.inventoryId = :inventoryId
                group by
                    ive.name,
                    ive.quantity
            ) as subquery
            """)
    Long countQuantityByDistinctNameByInventoryId(Long inventoryId);


    /**
     * delete by digital service uid
     *
     * @param digitalServiceUid the uid
     */
    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndNameIn(Long inventoryId, Set<String> names);

    List<InVirtualEquipment> findByDigitalServiceUid(final String digitalServiceUid, final Pageable pageable);

    long countByDigitalServiceUid(final String digitalServiceUid);

    long countByDigitalServiceUidAndInfrastructureType(final String digitalServiceUid, final String infrastructureType);

    long countByInventoryIdAndInfrastructureType(final Long inventoryId, final String infrastructureType);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndPhysicalEquipmentNameIn(Long inventoryId, Set<String> names);

    @Transactional
    @Modifying
    void deleteByInventoryId(Long inventoryId);

    /**
     * delete by digital service uid and Infrastructure type
     *
     * @param digitalServiceUid  uid
     * @param infrastructureType infrastructure type
     */
    @Transactional
    @Modifying
    void deleteByDigitalServiceUidAndInfrastructureType(final String digitalServiceUid, final String infrastructureType);

    /**
     * delete by inventory id and Infrastructure type
     *
     * @param inventoryId        id
     * @param infrastructureType infrastructure type
     */
    @Transactional
    @Modifying
    void deleteByInventoryIdAndInfrastructureType(final Long inventoryId, final String infrastructureType);

    /**
     * Find virtual equipments of one inventory and virtual equipment names
     *
     * @param inventoryId           inventory i
     * @param virtualEquipmentNames set of name
     * @return return a list of virtual equipments
     */
    @Query("SELECT v FROM InVirtualEquipment v WHERE v.inventoryId = :inventoryId AND v.name IN :names")
    List<InVirtualEquipment> findByInventoryIdAndVirtualEquipmentName(
            @Param("inventoryId") Long inventoryId,
            @Param("names") Collection<String> virtualEquipmentNames
    );
}
