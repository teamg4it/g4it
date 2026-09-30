/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.OutAiService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OutAiServiceRepository extends JpaRepository<OutAiService, Long> {

    @Transactional
    @Modifying
    void deleteByTaskId(Long taskId);

    List<OutAiService> findByTaskIdOrderByIdAsc(Long taskId, Pageable pageable);

    @Query("""
    SELECT DISTINCT o.source
    FROM OutAiService o
    WHERE o.taskId = :taskId
    AND o.source IS NOT NULL
    """)
    List<String> findDistinctSourcesByTaskId(@Param("taskId") Long taskId);
}