/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiinout.modeldb.OutPhysicalEquipment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Out OutPhysical Equipment JPA repository.
 */
@Repository
public interface OutPhysicalEquipmentRepository extends JpaRepository<OutPhysicalEquipment, Long> {

    List<OutPhysicalEquipment> findByTaskId(Long taskId);

    @Transactional
    @Modifying
    void deleteByTaskId(Long taskId);

    // In OutPhysicalEquipmentRepository
    @Query("SELECT o.criterion, o FROM OutPhysicalEquipment o WHERE o.taskId = :taskId")
    List<Object[]> findCriterionAndEquipmentByTaskId(@Param("taskId") Long taskId);


    @Query("""
    SELECT DISTINCT o.source
        FROM OutPhysicalEquipment o
        WHERE o.taskId = :taskId
        AND o.source IS NOT NULL
""")
    List<String> findDistinctSourcesByTaskId(@Param("taskId") Long taskId);


    /**
     * Populate level field from referential data.
     * Level is determined by the original equipment model/type from in_physical_equipment.
     * Uses the same three-tier priority as indicator views: model direct, matching_item, then type default.
     */
    @Transactional
    @Modifying
    @Query(value = """
        UPDATE out_physical_equipment ope
        SET level = (
            SELECT COALESCE(
                -- Priority 1: Direct model match in ref_item_impact
                (SELECT rii.level
                 FROM in_physical_equipment ipe
                 JOIN inventory inv ON inv.id = ipe.inventory_id
                 JOIN ref_item_impact rii ON rii.name = ipe.model
                 WHERE ipe.inventory_id = (SELECT inventory_id FROM task WHERE id = :taskId)
                   AND ope.equipment_type = ipe.type
                   AND rii.level IS NOT NULL
                   AND (rii.workspace_id = inv.organization_id OR rii.workspace_id IS NULL)
                 ORDER BY rii.workspace_id DESC NULLS LAST
                 LIMIT 1),
                -- Priority 2: Model via matching_item
                (SELECT rii.level
                 FROM in_physical_equipment ipe
                 JOIN inventory inv ON inv.id = ipe.inventory_id
                 JOIN ref_matching_item rmi ON rmi.item_source = ipe.model
                 JOIN ref_item_impact rii ON rii.name = rmi.ref_item_target
                 WHERE ipe.inventory_id = (SELECT inventory_id FROM task WHERE id = :taskId)
                   AND ope.equipment_type = ipe.type
                   AND rmi.subscriber IS NULL
                   AND (rmi.workspace_id = inv.organization_id OR rmi.workspace_id IS NULL)
                   AND rii.level IS NOT NULL
                   AND (rii.workspace_id = inv.organization_id OR rii.workspace_id IS NULL)
                 ORDER BY rmi.workspace_id DESC NULLS LAST, rii.workspace_id DESC NULLS LAST
                 LIMIT 1),
                -- Priority 3: Type default from ref_item_type
                (SELECT rii.level
                 FROM inventory inv
                 JOIN ref_item_type rit ON rit.type = ope.equipment_type
                 JOIN ref_item_impact rii ON rii.name = rit.ref_default_item
                 WHERE inv.id = (SELECT inventory_id FROM task WHERE id = :taskId)
                   AND rit.subscriber IS NULL
                   AND (rit.workspace_id = inv.organization_id OR rit.workspace_id IS NULL)
                   AND rii.level IS NOT NULL
                   AND (rii.workspace_id = inv.organization_id OR rii.workspace_id IS NULL)
                 ORDER BY rit.workspace_id DESC NULLS LAST, rii.workspace_id DESC NULLS LAST
                 LIMIT 1)
            )
        )
        WHERE ope.task_id = :taskId
    """, nativeQuery = true)
    void populateLevelFromReferential(@Param("taskId") Long taskId);
}

