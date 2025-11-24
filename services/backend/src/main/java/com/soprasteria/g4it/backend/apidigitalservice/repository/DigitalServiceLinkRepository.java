/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceSharedLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceVersion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalServiceLinkRepository extends JpaRepository<DigitalServiceSharedLink, String> {

    List<DigitalServiceSharedLink> findByDigitalServiceVersion(final DigitalServiceVersion digitalServiceVersion);

    @Modifying
    @Query("DELETE FROM DigitalServiceSharedLink d WHERE d.expiryDate < CURRENT_TIMESTAMP()")
    @Transactional
    int deleteExpiredLinks();

    @Query("SELECT d FROM DigitalServiceSharedLink d " +
            "JOIN FETCH d.digitalServiceVersion dsv " +
            "WHERE d.uid = :shareId " +
            "AND dsv.uid = :digitalServiceVersionId " +
            "AND d.expiryDate > CURRENT_TIMESTAMP")
    Optional<DigitalServiceSharedLink> validateLink(@Param("shareId") String shareId,
                                                    @Param("digitalServiceVersionId") String digitalServiceVersionId);

    boolean existsByDigitalServiceVersion_UidAndIsActiveTrue(String digitalServiceVersionUid);

    // Find link by its uid and the related DigitalService uid
    Optional<DigitalServiceSharedLink> findByUidAndDigitalServiceVersion_Uid(String shareId, String digitalServiceVersionUid);

}
