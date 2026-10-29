/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.repository;

import com.soprasteria.g4it.backend.apireferential.modeldb.RefNonItActivityImpact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefNonItActivityImpactRepository extends JpaRepository<RefNonItActivityImpact, Long> {

    /**
     * Find the workspace-specific impact factor for a reference and criterion.
     *
     * @param reference   the activity reference code
     * @param criterion   the criterion code
     * @param workspaceId the workspace id
     * @return the matching impact row, if any
     */
    Optional<RefNonItActivityImpact> findByReferenceAndCriterionAndWorkspaceId(String reference, String criterion, Long workspaceId);

    /**
     * Find the global (non workspace-specific) impact factor for a reference and criterion.
     *
     * @param reference the activity reference code
     * @param criterion the criterion code
     * @return the matching impact row, if any
     */
    Optional<RefNonItActivityImpact> findByReferenceAndCriterionAndWorkspaceIdIsNull(String reference, String criterion);
}
