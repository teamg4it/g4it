/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InDatacenter;
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
public interface InDatacenterRepository extends JpaRepository<InDatacenter, Long> {

    /**
     * Find datacenter by the functionally unique fields
     *
     * @param digitalServiceUid digital service Identifier
     * @param id                datacenter id
     * @return return a datacenter
     */
    Optional<InDatacenter> findByDigitalServiceUidAndId(String digitalServiceUid, Long id);

    /**
     * Find datacenters of one digital service
     *
     * @param digitalServiceUid digital service Identifier
     * @return return a list of datacenters
     */
    List<InDatacenter> findByDigitalServiceUid(String digitalServiceUid);

    /**
     * Find datacenter by the functionally unique fields
     *
     * @param inventoryId inventory id
     * @param id          datacenter id
     * @return return a datacenter
     */
    Optional<InDatacenter> findByInventoryIdAndId(Long inventoryId, Long id);

    /**
     * Find datacenters of one digital service
     *
     * @param inventoryId inventory id
     * @return return a list of datacenters
     */
    List<InDatacenter> findByInventoryId(Long inventoryId);

    @Transactional
    @Modifying
    void deleteByInventoryIdAndNameIn(Long inventoryId, Set<String> names);

    /**
     * Count distinct datacenter name by inventory id.
     *
     * @param inventoryId the unique inventory identifier.
     * @return the distinct datacenter number.
     */
    @Query("select count(distinct d.name) from InDatacenter d where d.inventoryId = :inventoryId")
    Long countDistinctNameByInventoryId(@Param("inventoryId") final Long inventoryId);

    @Transactional
    @Modifying
    void deleteByInventoryId(Long inventoryId);

    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);
}
