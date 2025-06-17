package com.soprasteria.g4it.backend.apiparameterai.repository;

import com.soprasteria.g4it.backend.apiparameterai.modeldb.InAiParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.util.List;

@Repository
public interface InAiParameterRepository extends JpaRepository<InAiParameter, BigInteger> {

    /**
     * Find by organization name and userId.
     *
     * @return DigitalService list.
     */
    InAiParameter findByDigitalServiceUid(final String digitalServiceUid);

}




