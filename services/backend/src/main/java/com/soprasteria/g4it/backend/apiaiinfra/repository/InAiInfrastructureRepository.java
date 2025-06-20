/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiaiinfra.repository;

import com.soprasteria.g4it.backend.apiaiinfra.modeldb.InAiInfrastructure;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface InAiInfrastructureRepository extends JpaRepository<InAiInfrastructure, Long> {

    /**
     * Find by digitalServiceUid.
     *
     * @return DigitalService list.
     */
    InAiInfrastructure findByDigitalServiceUid(final String digitalServiceUid);

    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);
}
