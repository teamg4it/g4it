/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Organization repository to access organization data in database.
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    /**
     * Find an organization by subscriber and organization name.
     *
     * @param subscriberName   the client subscriber.
     * @param organizationName the organization name.
     * @return the organization.
     */
    Optional<Workspace> findBySubscriberNameAndName(final String subscriberName, final String organizationName);

    /**
     * Find active organization by id.
     *
     * @param id     the organization id.
     * @param status the status.
     * @return the organization.
     */
    Optional<Workspace> findByIdAndStatusIn(final Long id, List<String> status);

    /**
     * Find all organizations by status.
     *
     * @param status the status.
     * @return the organization.
     */
    List<Workspace> findAllByStatusIn(List<String> status);

    /**
     * Find all organizations by isMigrated.
     *
     * @param isMigrated the status isMigrated.
     * @return the organization.
     */
    List<Workspace> findAllByIsMigrated(Boolean isMigrated);

    /**
     * Find organization by organization's name and subscriber's id.
     *
     * @param subscriberId     the subscriber's id
     * @param organizationName the organization's name
     * @return Organization
     */
    Optional<Workspace> findBySubscriberIdAndName(final Long subscriberId, final String organizationName);

    /**
     * Find organization by organization's id and subscriber's id and organization's statuses.
     *
     * @param organizationId the organization's id
     * @param subscriberId   the subscriber's id
     * @param status         the status.
     * @return Organization
     */
    Optional<Workspace> findByIdAndSubscriberIdAndStatusIn(final Long organizationId, final Long subscriberId, final List<String> status);

    /**
     * Update the status of organization
     *
     * @param id     the organization id
     * @param status the status
     */
    @Transactional
    @Modifying
    @Query("UPDATE Workspace org SET org.status = ?2 WHERE org.id = ?1")
    void setStatusForWorkspace(Long id, String status);

    /**
     * Find organization by subscriber's id.
     *
     * @param subscriberId the subscriber's id
     * @return Organization
     */
    List<Workspace> findBySubscriberId(final Long subscriberId);

}
