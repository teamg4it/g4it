package com.soprasteria.g4it.backend.apirecomandation.repository;
import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutAiRecoRepository extends JpaRepository<OutAiReco, Long> {
}


