/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InAiService;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * In AI Service JPA repository.
 */
@Repository
public interface InAiServiceRepository extends JpaRepository<InAiService, Long> {

    /**
     * Find AI services of one inventory
     *
     * @param inventoryId inventory id
     * @return return a list of AI services
     */
    List<InAiService> findByInventoryId(Long inventoryId);

    /**
     * Delete AI services of one inventory
     *
     * @param inventoryId inventory id
     */
    @Transactional
    @Modifying
    void deleteByInventoryId(Long inventoryId);

    /**
     * Delete AI services of one inventory matching the given service names.
     *
     * @param inventoryId  inventory id
     * @param serviceNames the service names
     */
    @Transactional
    @Modifying
    void deleteByInventoryIdAndServiceNameIn(Long inventoryId, Set<String> serviceNames);
}
