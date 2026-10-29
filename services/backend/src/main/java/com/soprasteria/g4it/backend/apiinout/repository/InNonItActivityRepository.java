/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.InNonItActivity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * In Non-IT Activity JPA repository.
 */
@Repository
public interface InNonItActivityRepository extends JpaRepository<InNonItActivity, Long> {

    /**
     * Find non-IT activities of one inventory
     *
     * @param inventoryId inventory id
     * @return return a list of non-IT activities
     */
    List<InNonItActivity> findByInventoryId(Long inventoryId);

    /**
     * Delete non-IT activities of one inventory
     *
     * @param inventoryId inventory id
     */
    @Transactional
    @Modifying
    void deleteByInventoryId(Long inventoryId);

    /**
     * Delete non-IT activities of one inventory matching the given activity names.
     *
     * @param inventoryId   inventory id
     * @param activityNames the activity names
     */
    @Transactional
    @Modifying
    void deleteByInventoryIdAndActivityNameIn(Long inventoryId, Set<String> activityNames);

    List<InNonItActivity> findByInventoryIdOrderByIdAsc(Long inventoryId, Pageable pageable);

    long countByInventoryId(Long inventoryId);
}
