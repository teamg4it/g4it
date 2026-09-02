/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Out Application JPA repository.
 */
@Repository
public interface OutApplicationRepository extends JpaRepository<OutApplication, Long>, OutApplicationCustomRepository {

    List<OutApplication> findByTaskId(Long taskId);

    /**
     * §4.4 - DB-side paginated listing (LIMIT/OFFSET) for the table view, avoiding
     * a full in-memory load/slice of the inventory's application indicators.
     */
    Page<OutApplication> findByTaskId(Long taskId, Pageable pageable);

}
