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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Digital Service repository.
 */
@Repository
public interface DigitalServiceVersionRepository extends JpaRepository<DigitalServiceVersion, String> {


    @Modifying
    @Transactional
    @Query("UPDATE DigitalServiceVersion dsv SET dsv.lastUpdateDate = ?1 where dsv.uid = ?2")
    void updateLastUpdateDate(LocalDateTime lastUpdateDate, String digitalServiceUid);


    @Query("SELECT dsv " +
            "FROM DigitalServiceVersion dsv " +
            "WHERE dsv.digitalService.uid IN :dsUids " +
            " AND dsv.versionType = 'active' ")
    List<DigitalServiceVersion> findActiveDigitalServiceVersion(List<String> dsUids);

    List<DigitalServiceVersion> findByDigitalServiceUid(String uid);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO digital_service_version (
                uid, description, last_calculation_date, creation_date,
                last_update_date, item_id, version_type, criteria, created_by
            )
            SELECT
                :newUid,
                description || ' (1)',
                NOW(),
                NOW(),
                NOW(),
                item_id,
                'draft',
                criteria,
                created_by
            FROM digital_service_version
            WHERE uid = :oldUid
            """, nativeQuery = true)
    void duplicateVersionRecord(@Param("oldUid") String oldUid, @Param("newUid") String newUid);


}
