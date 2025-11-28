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
import org.springframework.data.repository.query.Param;
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
     * @param digitalServiceVersionUid digital service Identifier
     * @param id                       physical equipment id
     * @return return a physical equipment
     */
    Optional<InPhysicalEquipment> findByDigitalServiceVersionUidAndId(String digitalServiceVersionUid, Long id);

    /**
     * Find physical equipments of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of physical equipments
     */
    List<InPhysicalEquipment> findByDigitalServiceUid(String digitalServiceUid);

    List<InPhysicalEquipment> findByDigitalServiceVersionUid(String digitalServiceVersionUid);

    List<InPhysicalEquipment> findByDigitalServiceVersionUid(String digitalServiceVersionUid, Pageable pageable);

    /**
     * Find physical equipments of one digital service order by name
     *
     * @param digitalServiceVersionUid digital service Identifier
     * @return return a list of physical equipments
     */
    List<InPhysicalEquipment> findByDigitalServiceVersionUidOrderByName(String digitalServiceVersionUid);

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

    long countByDigitalServiceVersionUid(String digitalServiceVersionUid);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndNameIn(Long inventoryId, Set<String> names);

    @Transactional
    @Modifying
    void deleteByDigitalServiceVersionUidAndNameIn(String digitalServiceVersionUid, Set<String> names);

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

    @Transactional
    @Modifying
    void deleteByDigitalServiceVersionUid(String digitalServiceVersionUid);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO in_physical_equipment (
                name,digital_service_uid,datacenter_name,location,quantity,type,model,manufacturer,
                date_purchase,date_withdrawal,cpu_type,cpu_core_number,size_disk_gb,size_memory_gb,
                source,quality,electricity_consumption,common_filters,filters,creation_date,last_update_date,
                duration_hour,description,nb_user,digital_service_version_uid
            )
            SELECT
                name, digital_service_uid,datacenter_name,location,quantity,type,model,manufacturer,
                date_purchase,date_withdrawal,cpu_type,cpu_core_number,size_disk_gb,size_memory_gb,
                source,quality,electricity_consumption,common_filters,filters,NOW(), NOW(),
                duration_hour,description,nb_user,:newUid
            FROM in_physical_equipment
            WHERE digital_service_version_uid = :oldUid
            """, nativeQuery = true)
    void copyForVersion(@Param("oldUid") String oldUid, @Param("newUid") String newUid);
}
