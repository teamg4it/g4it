package com.soprasteria.g4it.backend.apirecomandation.repository;

import com.soprasteria.g4it.backend.apirecomandation.modeldb.OutAiReco;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface OutAiRecoRepository extends JpaRepository<OutAiReco, Long> {

    OutAiReco findByTaskId(Long taskId);

    @Transactional
    @Modifying
    void deleteByTaskId(Long taskId);
}


