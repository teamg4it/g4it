/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InApplication;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * In Datacenter JPA repository.
 */
@Repository
public interface InApplicationRepository extends JpaRepository<InApplication, Long> {

    /**
     * Find application by the functionally unique fields
     *
     * @param digitalServiceUid digital service Identifier
     * @param id                application id
     * @return return a application
     */
    Optional<InApplication> findByDigitalServiceUidAndId(String digitalServiceUid, Long id);

    /**
     * Find applications of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of applications
     */
    List<InApplication> findByDigitalServiceUid(String digitalServiceUid);

    /**
     * Find application by the functionally unique fields
     *
     * @param inventoryId inventory id
     * @param id          application id
     * @return return a application
     */
    Optional<InApplication> findByInventoryIdAndId(Long inventoryId, Long id);

    /**
     * Find applications of one digital service
     *
     * @param inventoryId inventory id
     * @return return a list of applications
     */
    List<InApplication> findByInventoryId(Long inventoryId);

    List<InApplication> findByInventoryIdAndPhysicalEquipmentNameAndVirtualEquipmentName(Long inventoryId,
                                                                                         String physicalEquipmentName,
                                                                                         String virtualEquipmentName);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndNameIn(Long inventoryId, Set<String> names);

    /**
     * Count distinct application name by inventory id.
     *
     * @param inventoryId the unique inventory identifier.
     * @return the distinct application number.
     * @implNote Use JPQL query because Spring Data generated a select count distinct id.
     */
    @Query("select count(distinct a.name) from InApplication a where a.inventoryId = :inventoryId")
    Long countDistinctNameByInventoryId(@Param("inventoryId") final Long inventoryId);

    @Transactional
    @Modifying
    void deleteByInventoryId(Long inventoryId);
}
