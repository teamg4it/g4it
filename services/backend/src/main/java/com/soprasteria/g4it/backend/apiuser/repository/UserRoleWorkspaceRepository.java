/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.UserRoleWorkspace;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleWorkspaceRepository extends JpaRepository<UserRoleWorkspace, Long> {

    @Transactional
    void deleteByUserWorkspaces(final UserWorkspace userWorkspace);
}
