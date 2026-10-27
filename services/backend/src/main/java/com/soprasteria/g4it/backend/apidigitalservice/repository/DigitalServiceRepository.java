/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
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
 * Digital Service repository.
 */
@Repository
public interface DigitalServiceRepository extends JpaRepository<DigitalService, String> {

    /**
     * Find by workspace name
     *
     * @param workspace the linked workspace.
     * @return DigitalService list.
     */
    List<DigitalService> findByWorkspace(final Workspace workspace);

    /**
     * @param workspace the linked workspace.
     * @param isAi      is DS an Ai or not
     * @return DigitalService list.
     */
    List<DigitalService> findByWorkspaceAndIsAi(Workspace workspace, Boolean isAi);

    /**
     * Find by workspace and the digitalServiceUid and return the matching digitalService
     *
     * @param workspace the unique workspace identifier.
     * @return matching digitalService
     */
    Optional<DigitalService> findByWorkspaceAndUid(final Workspace workspace,
                                                   final String digitalServiceUid);

    @Modifying
    @Transactional
    @Query("UPDATE DigitalService ds SET ds.lastUpdateDate = ?1 where ds.uid = ?2")
    void updateLastUpdateDate(LocalDateTime lastUpdateDate, String digitalServiceUid);

    /**
     * Delete a digital service by its uid using a single native statement.
     * Unlike {@link JpaRepository#deleteById}, this does NOT load the entity nor its eager
     * collections (tasks, digital service versions) into the persistence context beforehand,
     * and does NOT throw when the row no longer exists (idempotent, safe to retry).
     * Child rows (digital_service_version, task, digital_service_shared_link, note) are removed
     * by the database itself through the existing "ON DELETE CASCADE" foreign keys, in one round trip,
     * instead of Hibernate issuing one DELETE statement per child row.
     *
     * @param digitalServiceUid the digital service uid.
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM digital_service WHERE uid = :digitalServiceUid", nativeQuery = true)
    void deleteByUidNative(@Param("digitalServiceUid") String digitalServiceUid);

}
