/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.repository;

import com.soprasteria.g4it.backend.apireferential.modeldb.ItemImpact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ItemImpactRepository extends JpaRepository<ItemImpact, Long> {

    @Query("""
            SELECT cf FROM #{#entityName} cf WHERE
            ((?1 IS NULL) OR (?1 IS NOT NULL AND cf.criterion = ?1)) AND
            ((?2 IS NULL) OR (?2 IS NOT NULL AND cf.lifecycleStep = ?2)) AND
            ((?3 IS NULL) OR (?3 IS NOT NULL AND cf.name = ?3)) AND
            ((?4 IS NULL) OR (?4 IS NOT NULL AND cf.category = ?4)) AND
            ((?5 IS NULL) OR (?5 IS NOT NULL AND cf.location = ?5)) AND
            ((?6 IS NULL) OR (?6 IS NOT NULL AND cf.organization = ?6))
            """)
    List<ItemImpact> findByCriterionAndLifecycleStepAndNameAndCategoryAndLocationAndOrganization(final String criterion,
                                                                                                 final String lifecycleStep,
                                                                                                 final String name,
                                                                                                 final String category,
                                                                                                 final String location,
                                                                                                 final String organization);

    List<ItemImpact> findByLevel(final String level);

    List<ItemImpact> findByCategory(final String category);

    Page<ItemImpact> findByOrganization(final String organization, final Pageable pageable);

    @Query("""
            SELECT distinct ii.location
            FROM ItemImpact ii
            WHERE ii.category = 'electricity-mix'
            AND (:organization IS NULL OR :organization IS NOT NULL AND ii.organization = :organization)
            """)
    List<String> findCountries(@Param("organization") String organization);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE ref_item_impact", nativeQuery = true)
    void truncateTable();

    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} ii WHERE (?1 IS NULL) OR (?1 IS NOT NULL AND ii.organization = ?1)")
    void deleteByOrganization(final String organization);
}
