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
 * workspace repository to access workspace data in database.
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    /**
     * Find a workspace by organization and workspace name.
     *
     * @param organizationName the client organization.
     * @param workspaceName    the workspace name.
     * @return the workspace.
     */
    Optional<Workspace> findByOrganizationNameAndName(final String organizationName, final String workspaceName);

    /**
     * Find active workspace by id.
     *
     * @param id     the workspace id.
     * @param status the status.
     * @return the workspace.
     */
    Optional<Workspace> findByIdAndStatusIn(final Long id, List<String> status);

    /**
     * Find all workspaces by status.
     *
     * @param status the status.
     * @return the workspace.
     */
    List<Workspace> findAllByStatusIn(List<String> status);

    /**
     * Find all workspaces by isMigrated.
     *
     * @param isMigrated the status isMigrated.
     * @return the workspace.
     */
    List<Workspace> findAllByIsMigrated(Boolean isMigrated);

    /**
     * Find workspace by workspace's name and organization's id.
     *
     * @param organizationId the organization's id
     * @param workspaceName  the workspace's name
     * @return workspace
     */
    Optional<Workspace> findByOrganizationIdAndName(final Long organizationId, final String workspaceName);

    /**
     * Find workspace by workspace's id and organization's id and workspace's statuses.
     *
     * @param workspaceId    the workspace's id
     * @param organizationId the organization's id
     * @param status         the status.
     * @return workspace
     */
    Optional<Workspace> findByIdAndOrganizationIdAndStatusIn(final Long workspaceId, final Long organizationId, final List<String> status);

    /**
     * Update the status of workspace
     *
     * @param id     the workspace id
     * @param status the status
     */
    @Transactional
    @Modifying
    @Query("UPDATE Workspace org SET org.status = ?2 WHERE org.id = ?1")
    void setStatusForWorkspace(Long id, String status);

    /**
     * Find workspace by organization's id.
     *
     * @param organizationId the organization's id
     * @return workspace
     */
    List<Workspace> findByOrganizationId(final Long organizationId);

}
