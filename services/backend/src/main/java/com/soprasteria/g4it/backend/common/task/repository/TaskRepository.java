/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Task repository.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusAndType(final String status, final String type);

    List<Task> findByInventoryAndType(final Inventory inventory, final String type);
    List<Task> findByDigitalServiceUidAndType(final  String digitalServiceUid, final String type);

    List<Task> findByInventoryAndStatusAndType(final Inventory inventory, final String status, final String type);

    Optional<Task> findByDigitalServiceUid(final String digitalServiceUid);

    /**
     * Find by digitalServiceUid
     *
     * @param digitalServiceUid the digitalServiceUid
     * @return task linked to digital service
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.digitalServiceUid = :digitalServiceUid AND type = 'EVALUATING_DIGITAL_SERVICE'
            ORDER BY creationDate DESC LIMIT 1
            """)
    Optional<Task> findByDigitalServiceUidAndLastCreationDate(@Param("digitalServiceUid") final String digitalServiceUid);

    /**
     * Find by inventory id
     *
     * @param inventory inventory
     * @return task linked to inventory id
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.inventory = :inventory AND type = 'EVALUATING'
            ORDER BY creationDate DESC LIMIT 1
            """)
    Optional<Task> findByInventoryAndLastCreationDate(@Param("inventory") final Inventory inventory);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Task t SET t.lastUpdateDate = :lastUpdateDate
            WHERE t.id = :taskId
            """)
    void updateLastUpdateDate(@Param("taskId") final Long taskId, @Param("lastUpdateDate") final LocalDateTime lastUpdateDate);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Task t
            WHERE t.id = :taskId
            """)
    void deleteTask(@Param("taskId") final Long taskId);

    List<Task> findByTypeAndInventoryId(String type, Long inventoryId);

}
