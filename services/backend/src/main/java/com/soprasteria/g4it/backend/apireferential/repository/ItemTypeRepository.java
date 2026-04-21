/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.repository;

import com.soprasteria.g4it.backend.apireferential.modeldb.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, Long> {

    Optional<ItemType> findByTypeAndOrganization(final String type, final String organization);

    List<ItemType> findByOrganization(final String organization);

    @Transactional
    @Modifying
    void deleteByOrganization(final String organization);

    List<ItemType> findByWorkspaceId(Long workspaceId);

    Optional<ItemType> findByTypeAndWorkspaceId(String type, Long workspaceId);

    @Transactional
    @Modifying
    void deleteByWorkspaceId(Long workspaceId);

    List<ItemType> findByWorkspaceIdOrWorkspaceIdIsNull(Long workspaceId);

    Page<ItemType> findByWorkspaceId(Long workspaceId, Pageable pageable);
}
