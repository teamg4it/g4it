package com.soprasteria.g4it.backend.apiparameterai.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.util.List;

@Repository
public interface AiParameterRepository extends JpaRepository<AiParameter, BigInteger> {

    /**
     * Find by organization name and userId.
     *
     * @return DigitalService list.
     */
    List<AiParameter> findByDigitalServiceUid(final String digitalServiceUid);

}




