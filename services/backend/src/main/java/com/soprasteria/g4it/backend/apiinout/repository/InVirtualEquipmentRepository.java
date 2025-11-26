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
     * @param digitalServiceVersionUid digital service Identifier
     * @param id                       virtual equipment id
     * @return return a virtual equipment
     */
    Optional<InVirtualEquipment> findByDigitalServiceVersionUidAndId(String digitalServiceVersionUid, Long id);

    /**
     * Find virtual equipments of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceUid(String digitalServiceUid);

    /**
     * Find virtual equipments of one digital service
     *
     * @param digitalServiceVersionUid digital service Identifier
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceVersionUid(String digitalServiceVersionUid);

    /**
     * Find virtual equipments of one digital service order by name
     *
     * @param digitalServiceVersionUid digital service Identifier
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceVersionUidOrderByName(String digitalServiceVersionUid);

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
     * @param digitalServiceVersionUid digitalServiceUid
     * @param physicalEquipmentName    physicalEquipmentName
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceVersionUidAndPhysicalEquipmentName(String digitalServiceVersionUid, String physicalEquipmentName, Pageable pageable);

    /**
     * Find virtual equipments of one inventory and one physical equipment name
     *
     * @param digitalServiceVersionUid digitalServiceUid
     * @param physicalEquipmentName    physicalEquipmentName
     * @return return a list of virtual equipments
     */
    List<InVirtualEquipment> findByDigitalServiceVersionUidAndPhysicalEquipmentName(String digitalServiceVersionUid, String physicalEquipmentName);

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

    @Transactional
    @Modifying
    void deleteByDigitalServiceUidAndNameIn(String digitalServiceUid, Set<String> names);

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

    @Transactional
    @Modifying
    void deleteByDigitalServiceVersionUid(String digitalServiceVersionUid);

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

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO in_virtual_equipment (
                name, inventory_id, digital_service_uid, datacenter_name, physical_equipment_name,
                quantity, infrastructure_type, instance_type, type, provider, location, duration_hour,
                workload, electricity_consumption, vcpu_core_number, size_memory_gb, size_disk_gb,
                allocation_factor, common_filters, filters, creation_date, last_update_date, digital_service_version_uid
            )
            SELECT
                name, inventory_id, digital_service_uid, datacenter_name, physical_equipment_name,
                quantity, infrastructure_type, instance_type, type, provider, location, duration_hour,
                workload, electricity_consumption, vcpu_core_number, size_memory_gb, size_disk_gb,
                allocation_factor, common_filters, filters, NOW(), NOW(), :newUid
            FROM in_virtual_equipment
            WHERE digital_service_version_uid = :oldUid
            """, nativeQuery = true)
    void copyForVersion(@Param("oldUid") String oldUid, @Param("newUid") String newUid);
}
