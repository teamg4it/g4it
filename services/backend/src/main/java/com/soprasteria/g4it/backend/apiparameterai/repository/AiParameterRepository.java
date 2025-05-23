package com.soprasteria.g4it.backend.apiparameterai.repository;

import com.soprasteria.g4it.backend.apiparameterai.modeldb.AiParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;

@Repository
public interface AiParameterRepository extends JpaRepository<AiParameter, BigInteger> {


}


