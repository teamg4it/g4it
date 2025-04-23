/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiloadinputfiles.repository;

import com.soprasteria.g4it.backend.apiloadinputfiles.dto.DuplicateEquipmentDTO;
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckDatacenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CheckDatacenter JPA repository.
 */
@Repository
public interface CheckDatacenterRepository extends JpaRepository<CheckDatacenter, Long> {

    /**
     * Find datacenter metadata by the task id
     *
     * @param taskId task id
     * @return return the checkDatacenters
     */
    List<CheckDatacenter> findByTaskId(Long taskId);

    @Query(nativeQuery = true, value = """
            SELECT cd.datacenter_name  as equipmentName,
                                   STRING_AGG(cd.filename || ':' || cd.line_nb, ',') as filenameLineInfo
                            FROM check_inv_load_datacenter cd
                            WHERE cd.task_id = :taskId
                            GROUP BY cd.datacenter_name 
                            HAVING COUNT(*) > 1
                            LIMIT 50000
            """)
    List<DuplicateEquipmentDTO> findDuplicateDatacenters(@Param("taskId") Long taskId);
}
