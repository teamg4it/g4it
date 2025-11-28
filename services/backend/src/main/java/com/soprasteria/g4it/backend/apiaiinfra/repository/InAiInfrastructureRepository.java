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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InAiInfrastructureRepository extends JpaRepository<InAiInfrastructure, Long> {

    /**
     * Find by digitalServiceUid.
     *
     * @return DigitalService list.
     */
    InAiInfrastructure findByDigitalServiceUid(final String digitalServiceUid);

    InAiInfrastructure findByDigitalServiceVersionUid(final String digitalServiceVersionUid);

    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);

    @Transactional
    @Modifying
    void deleteByDigitalServiceVersionUid(String digitalServiceVersionUid);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO in_ai_infrastructure (
                complementary_pue, infrastructure_type, nb_gpu, gpu_memory, digital_service_uid, digital_service_version_uid
            )
            SELECT
                complementary_pue, infrastructure_type, nb_gpu, gpu_memory, digital_service_uid, :newUid
            FROM in_ai_infrastructure
            WHERE digital_service_version_uid = :oldUid
            """, nativeQuery = true)
    void copyForVersion(@Param("oldUid") String oldUid, @Param("newUid") String newUid);
}
