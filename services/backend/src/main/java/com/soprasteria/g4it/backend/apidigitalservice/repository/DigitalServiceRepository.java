/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
     * Find by organization name
     *
     * @param organization the linked organization.
     * @return DigitalService list.
     */
    List<DigitalService> findByOrganization(final Organization organization);

    /**
     * Find by organization and the digitalServiceUid and return the matching digitalService
     *
     * @param organization the unique organization identifier.
     * @return matching digitalService
     */
    Optional<DigitalService> findByOrganizationAndUid(final Organization organization,
                                                final String digitalServiceUid);

    @Modifying
    @Transactional
    @Query("UPDATE DigitalService ds SET ds.lastUpdateDate = ?1 where ds.uid = ?2")
    void updateLastUpdateDate(LocalDateTime lastUpdateDate, String digitalServiceUid);
}
