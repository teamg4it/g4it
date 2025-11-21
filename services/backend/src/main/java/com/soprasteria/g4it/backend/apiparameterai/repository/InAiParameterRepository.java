package com.soprasteria.g4it.backend.apiparameterai.repository;

import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

}




