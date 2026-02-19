package com.soprasteria.g4it.backend.apirecommendation.repository;

import com.soprasteria.g4it.backend.apirecommendation.modeldb.Recommendation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long>{

    List<Recommendation> findByOrganisationId(Long organisationId);

    @Transactional
    @Modifying
    void deleteByOrganisationId(Long organisationId);

}
