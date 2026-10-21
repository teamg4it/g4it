/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.repository;

import com.soprasteria.g4it.backend.apiadministrator.modeldb.ReleaseNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseNotesRepository extends JpaRepository<ReleaseNote, Long> {
    Optional<ReleaseNote> findFirstByOrderByIdDesc();

    @Query("""
            SELECT releaseNote
            FROM ReleaseNote releaseNote, User u
            WHERE u.id = :userId
              AND releaseNote.id = (SELECT MAX(latest.id) FROM ReleaseNote latest)
              AND (u.lastSeenReleaseNoteId IS NULL OR u.lastSeenReleaseNoteId <> releaseNote.id)
            """)
    Optional<ReleaseNote> findLatestUnseenByUserId(@Param("userId") long userId);
}
