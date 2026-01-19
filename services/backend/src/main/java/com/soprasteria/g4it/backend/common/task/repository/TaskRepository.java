/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.task.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
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

    List<Task> findByInventoryAndStatusAndType(final Inventory inventory, final String status, final String type);

    List<Task> findByDigitalServiceVersionAndStatusAndType(final DigitalServiceVersion digitalServiceVersion, final String status, final String type);

    Optional<Task> findByDigitalService(final DigitalService digitalService);

    List<Task> findByDigitalServiceVersionAndType(final DigitalServiceVersion digitalServiceVersion, final String type);

    Optional<Task> findByDigitalServiceVersion(final DigitalServiceVersion digitalServiceVersion);

    /**
     * Find by digitalService
     *
     * @param digitalServiceVersion the digitalService
     * @return task linked to digital service
     */
    @Query("""
            SELECT t FROM Task t
            WHERE t.digitalServiceVersion = :digitalServiceVersion AND type = 'EVALUATING_DIGITAL_SERVICE'
            ORDER BY creationDate DESC LIMIT 1
            """)
    Optional<Task> findByDigitalServiceVersionAndLastCreationDate(@Param("digitalServiceVersion") final DigitalServiceVersion digitalServiceVersion);

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

    @Modifying
    @Transactional
    @Query("""
                update Task t
                set t.progressPercentage = :progress,
                    t.lastUpdateDate = :date
                where t.id = :id
            """)
    void updateProgress(@Param("id") Long id,
                        @Param("progress") String progress,
                        @Param("date") LocalDateTime date);

    @Modifying
    @Transactional
    @Query("""
                update Task t
                set t.status = :status,
                    t.lastUpdateDate = :date,
                    t.progressPercentage = :progress
                where t.id = :id
            """)
    void updateTaskState(@Param("id") Long id,
                         @Param("status") String status,
                         @Param("date") LocalDateTime date,
                         @Param("progress") String progress);

    @Modifying
    @Transactional
    @Query("""
                update Task t
                set t.status = :status,
                    t.progressPercentage = :progress,
                    t.details = :details,
                    t.lastUpdateDate = CURRENT_TIMESTAMP
                where t.id = :taskId
            """)
    void updateTaskFinalState(
            Long taskId,
            String status,
            String progress,
            List<String> details
    );

    @Modifying
    @Query("""
            update Task t
            set t.status = :status,
                t.lastUpdateDate = :lastUpdateDate,
                t.progressPercentage = :progress,
                t.details = :details
            where t.id = :taskId """)
    void updateTaskStateWithDetails(@Param("taskId") Long taskId,
                                    @Param("status") String status,
                                    @Param("lastUpdateDate") LocalDateTime lastUpdateDate,
                                    @Param("progress") String progress,
                                    @Param("details") List<String> details);

}
