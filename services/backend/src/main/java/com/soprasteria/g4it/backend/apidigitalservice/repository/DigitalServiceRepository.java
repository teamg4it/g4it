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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
     * Find by organization name and userId.
     *
     * @param organization the linked organization.
     * @param userId       the userId to find.
     * @return DigitalService list.
     */
    List<DigitalService> findByOrganizationAndUserId(final Organization organization, final long userId);

    /**
     * Verify if the digitalService exists by the uid and userId.
     *
     * @param uid    the uid.
     * @param userId the userId to find.
     * @return the boolean.
     */
    @Cacheable("existsByUidAndUserId")
    boolean existsByUidAndUserId(final String uid, final long userId);

    @Modifying
    @Transactional
    @Query("UPDATE DigitalService ds SET ds.lastUpdateDate = ?1 where ds.uid = ?2")
    void updateLastUpdateDate(LocalDateTime lastUpdateDate, String digitalServiceUid);

    /**
     * Find by new arch false
     *
     * @return a list of matching digital services
     */
    List<DigitalService> findByIsNewArchFalse();

    long countByIsNewArch(Boolean isNewArch);
}
