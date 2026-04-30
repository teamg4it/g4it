package com.soprasteria.g4it.backend.apirecommendationds.repository;

import com.soprasteria.g4it.backend.apirecommendationds.modeldb.InstantiatedRecommendation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstantiatedRecommendationRepository extends JpaRepository<InstantiatedRecommendation, Long>{

    List<InstantiatedRecommendation> findByDigitalServiceVersionUid(String digitalServiceVersionUid);

    @Transactional
    @Modifying
    void deleteByDigitalServiceVersionUid(String digitalServiceVersionUid);

}