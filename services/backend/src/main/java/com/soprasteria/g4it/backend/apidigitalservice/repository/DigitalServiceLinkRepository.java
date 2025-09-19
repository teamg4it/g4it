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
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DigitalServiceLinkRepository extends JpaRepository<DigitalServiceSharedLink, String> {

    DigitalServiceSharedLink findByDigitalService(final DigitalService digitalService);

    @Modifying
    @Query("DELETE FROM DigitalServiceSharedLink d WHERE d.expiryDate < CURRENT_TIMESTAMP()")
    @Transactional
    int deleteExpiredLinks();
}
