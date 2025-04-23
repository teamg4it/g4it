/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends AbstractValidationBaseEntityRepository<Application>,
        JpaRepository<Application, Long> {

    /**
     * Find application by the functionally unique fields
     *
     * @param inventoryId       id of the inventory
     * @param nomApplication    application name
     * @param typeEnvironnement env type (DEV, INT, PROD..)
     * @param nomVM             VM name
     * @return return a list of application
     */
    Optional<Application> findByInventoryIdAndNomApplicationAndTypeEnvironnementAndNomEquipementVirtuel(
            final long inventoryId, final String nomApplication, final String typeEnvironnement,
            final String nomVM);

    /**
     * Find application not linked to a Virtual Equipment.
     *
     * @param sessionDate the session date.
     * @param pageable    pagination.
     * @return applications not linked to a Virtual Equipment.
     */
    @Query(value = """
            select * from application a where a.session_date = :sessionDate and not exists (
            select * from equipement_virtuel ev
            where a.inventory_id = ev.inventory_id
            and a.nom_vm = ev.nom_vm)
             """, nativeQuery = true)
    Page<Application> findApplicationNotLinkedToVirtualEquipment(final Date sessionDate,
                                                                 final Pageable pageable);

    /**
     * Count distinct application name from application and inApplication table.
     *
     * @param inventoryId the unique inventory identifier.
     * @return the distinct application number.
     * @implNote Use JPQL query because Spring Data generated a select count distinct id.
     */
    @Query(value = """
            select count(*) as total_count
            from (
                   select name
                   from (
                         select nom_application as name
                         from application
                         where inventory_id = :inventoryId
                         union
                         select name
                         from in_application
                         where inventory_id = :inventoryId
                        ) combined_apps
                  ) count_query
            """, nativeQuery = true)
    Long countDistinctCloudAndNonCloudApp(@Param("inventoryId") final Long inventoryId);

    /**
     * Delete application not linked to inventory or virtual equipments.
     *
     * @param sessionDate the session date.
     */
    @Modifying
    @Query(value = """
            delete from application a where a.session_date = :sessionDate and not exists (
            select * from inventory i
            where i.id = a.inventory_id)
            or not exists (
            select * from equipement_virtuel ev
            where a.inventory_id = ev.inventory_id
            and a.nom_vm = ev.nom_vm)
            """, nativeQuery = true)
    void deleteAfterConsistencyControl(final Date sessionDate);

    /**
     * Count distinct application name by inventory id.
     *
     * @param inventoryId the unique inventory identifier.
     * @return the distinct application number.
     * @implNote Use JPQL query because Spring Data generated a select count distinct id.
     */
    @Query(value = "select count(distinct a.nomApplication) from Application a where a.inventoryId = :inventoryId")
    long countDistinctNomApplicationByInventoryId(@Param("inventoryId") final Long inventoryId);

    List<Application> findByInventoryId(long inventoryId);

}
