package com.soprasteria.g4it.backend.apirecommendation.repository;

import com.soprasteria.g4it.backend.apirecommendation.modeldb.InstantiatedRecommendation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstantiatedRecommendationRepository extends JpaRepository<InstantiatedRecommendation, Long>{

    List<InstantiatedRecommendation> findByIdEvaluation(Long idEvaluation);

    @Transactional
    @Modifying
    void deleteByIdEvaluation(Long idEvaluation);

}