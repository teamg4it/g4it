/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.repository;

import com.soprasteria.g4it.backend.apireferential.modeldb.MatchingItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MatchingItemRepository extends JpaRepository<MatchingItem, Long> {
    Optional<MatchingItem> findByItemSourceAndOrganization(final String itemSource, final String organization);

    Page<MatchingItem> findByOrganization(final String organization, final Pageable pageable);

    @Transactional
    @Modifying
    void deleteByOrganization(final String organization);
}
