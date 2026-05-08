/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Out OutPhysical Equipment JPA repository.
 */
@Repository
public interface OutPhysicalEquipmentRepository extends JpaRepository<OutPhysicalEquipment, Long> {

    List<OutPhysicalEquipment> findByTaskId(Long taskId);

    @Transactional
    @Modifying
    void deleteByTaskId(Long taskId);

    @Query("""
    SELECT DISTINCT o.source
        FROM OutPhysicalEquipment o
        WHERE o.taskId = :taskId
        AND o.source IS NOT NULL
""")
    List<String> findDistinctSourcesByTaskId(@Param("taskId") Long taskId);
}
