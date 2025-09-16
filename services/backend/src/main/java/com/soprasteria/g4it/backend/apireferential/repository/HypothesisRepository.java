/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.repository;

import com.soprasteria.g4it.backend.apireferential.modeldb.Hypothesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface HypothesisRepository extends JpaRepository<Hypothesis, Long> {

    List<Hypothesis> findByOrganization(final String organization);

    @Transactional
    @Modifying
    void deleteByOrganization(final String organization);
}
