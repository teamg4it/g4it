/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.repository;

import com.soprasteria.g4it.backend.apiloadinputfiles.dto.DuplicateEquipmentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckPhysicalEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CheckPhysicalEquipment  JPA repository.
 */
@Repository
public interface CheckPhysicalEquipmentRepository extends JpaRepository<CheckPhysicalEquipment, Long> {
    /**
     * Find physical Equipment metadata by the task id
     *
     * @param taskId task id
     * @return return the checkPhysicalEquipments
     */
    List<CheckPhysicalEquipment> findByTaskId(Long taskId);

    /**
     * Retrieve duplicate physical equipment with details by task id.
     *
     * @param taskId the task id
     * @return List of DuplicateEquipmentDTO containing physical equipment names and their file name and line number
     */
    @Query(nativeQuery = true, value = """
            SELECT cpe.physical_equipment_name  as equipmentName,
                                   STRING_AGG(cpe.filename || ':' || cpe.line_nb, ',') as filenameLineInfo
                            FROM check_inv_load_physical_equipment cpe
                            WHERE cpe.task_id = :taskId
                            GROUP BY cpe.physical_equipment_name 
                            HAVING COUNT(*) > 1
                            LIMIT 50000
            """)
    List<DuplicateEquipmentDTO> findDuplicatePhysicalEquipments(@Param("taskId") Long taskId);
}

