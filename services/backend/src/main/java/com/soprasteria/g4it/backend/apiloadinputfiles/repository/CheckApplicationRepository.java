/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.repository;

import com.soprasteria.g4it.backend.apiloadinputfiles.dto.CoherenceParentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.dto.DuplicateEquipmentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CheckApplication JPA repository.
 */
@Repository
public interface CheckApplicationRepository extends JpaRepository<CheckApplication, Long> {

    static final String INCOHERENCE_APPLICATION_REQUEST = """
            select filename,line_nb as lineNb,virtual_equipment_name as parentEquipmentName, application_name as equipmentName from check_inv_load_application cila where
            not exists (select virtual_equipment_name from in_virtual_equipment ive where ive.inventory_id = :inventoryId  and cila.virtual_equipment_name = ive.name )
            and cila.task_id = :taskId
            """;

    static final String INCOHERENCE_APPLICATION_REQUEST_NOT_EXIST_PARENT_WITH_EXCEPTION = " and not exists (select virtual_equipment_name from check_inv_load_virtual_equipment cilve where cilve.task_id = cila.task_id and cila.virtual_equipment_name = cilve.virtual_equipment_name and cilve.virtual_equipment_name not in (:parentDuplicates) and cila.virtual_equipment_name is not null)";

    static final String INCOHERENCE_APPLICATION_REQUEST_NOT_EXIST_PARENT = " and not exists (select virtual_equipment_name from check_inv_load_virtual_equipment cilve where cilve.task_id = cila.task_id and cila.virtual_equipment_name = cilve.virtual_equipment_name and cila.virtual_equipment_name is not null)";


    /**
     * Find application metadata by the task id
     *
     * @param taskId task id
     * @return return the checkApplications
     */
    List<CheckApplication> findByTaskId(Long taskId);

    @Query(nativeQuery = true, value = """
            SELECT
                    CONCAT(ca.application_name, ', ', ca.environment_type, ', ', ca.virtual_equipment_name) as equipmentName,
                    STRING_AGG(CONCAT(ca.filename, ':', ca.line_nb), ',') as filenameLineInfo
                FROM check_inv_load_application ca
                WHERE ca.task_id = :taskId
                GROUP BY ca.application_name, ca.environment_type, ca.virtual_equipment_name
                HAVING COUNT(*) > 1
                LIMIT 50000
            """)
    List<DuplicateEquipmentDTO> findDuplicateApplications(@Param("taskId") Long taskId);

    @Query(nativeQuery = true, value = INCOHERENCE_APPLICATION_REQUEST + INCOHERENCE_APPLICATION_REQUEST_NOT_EXIST_PARENT_WITH_EXCEPTION)
    List<CoherenceParentDTO> findIncoherentApplications(@Param("taskId") Long taskId, @Param("inventoryId") Long inventoryId, @Param("parentDuplicates") List<String> parentDuplicates);

    @Query(nativeQuery = true, value = INCOHERENCE_APPLICATION_REQUEST + INCOHERENCE_APPLICATION_REQUEST_NOT_EXIST_PARENT)
    List<CoherenceParentDTO> findIncoherentApplications(@Param("taskId") Long taskId, @Param("inventoryId") Long inventoryId);

}
