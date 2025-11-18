/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Digital Service repository.
 */
@Repository
public interface DigitalServiceVersionRepository extends JpaRepository<DigitalServiceVersion, String> {
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO digital_service_version (
                uid, description, last_calculation_date, creation_date,
                last_update_date, item_id, version_type, criteria, created_by,task_id
            )
            SELECT
                :newUid,
                description || ' (1)',
                NOW(),
                NOW(),
                NOW(),
                item_id,
                'Draft',
                criteria,
                created_by,
                task_id
            FROM digital_service_version
            WHERE uid = :oldUid
            """, nativeQuery = true)
    void duplicateVersionRecord(@Param("oldUid") String oldUid, @Param("newUid") String newUid);


}