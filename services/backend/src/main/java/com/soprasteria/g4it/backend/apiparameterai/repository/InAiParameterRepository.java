package com.soprasteria.g4it.backend.apiparameterai.repository;

import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface InAiParameterRepository extends JpaRepository<InAiParameter, BigInteger> {

    /**
     * Find by organization name and userId.
     *
     * @return DigitalService list.
     */
    InAiParameter findByDigitalServiceUid(final String digitalServiceUid);

    /**
     * Find by organization name and userId.
     *
     * @return DigitalService list.
     */
    InAiParameter findByDigitalServiceVersionUid(final String digitalServiceVersionUid);

    @Transactional
    @Modifying
    void deleteByDigitalServiceUid(String digitalServiceUid);


    @Transactional
    @Modifying
    void deleteByDigitalServiceVersionUid(String digitalServiceVersionUid);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO in_ai_parameters (
                nb_parameters, framework, quantization, total_generated_tokens, number_user_year, average_number_request,
                average_number_token, is_inference, is_finetuning, creation_date, last_update_date, digital_service_uid,
                model_name, type, digital_service_version_uid
            )
            SELECT
                nb_parameters, framework, quantization, total_generated_tokens, number_user_year, average_number_request,
                average_number_token, is_inference, is_finetuning, NOW(), NOW(), digital_service_uid,
                model_name, type, :newUid
            FROM in_ai_parameters
            WHERE digital_service_version_uid = :oldUid
            """, nativeQuery = true)
    void copyForVersion(@Param("oldUid") String oldUid, @Param("newUid") String newUid);

}




