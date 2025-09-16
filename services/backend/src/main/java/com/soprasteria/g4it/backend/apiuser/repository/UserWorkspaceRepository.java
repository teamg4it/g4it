/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWorkspaceRepository extends JpaRepository<UserWorkspace, Long> {

    /**
     * Find by organization and return  a list of matching UserOrganizations
     *
     * @param workspace the linked organization
     * @return UserOrganization
     */
    List<UserWorkspace> findByWorkspace(final Workspace workspace);

    /**
     * Find by linked organizationId and userId  and return  a list of matching UserOrganizations
     *
     * @param workspaceId the linked organization's id
     * @param userId      the linked user's id
     * @return UserOrganization
     */
    Optional<UserWorkspace> findByWorkspaceIdAndUserId(final Long workspaceId, final Long userId);

}
