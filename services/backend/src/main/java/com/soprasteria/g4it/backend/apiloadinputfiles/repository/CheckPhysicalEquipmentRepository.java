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
                              AND cpe.physical_equipment_name IS NOT NULL                            
                            GROUP BY cpe.physical_equipment_name 
                            HAVING COUNT(*) > 1
                            LIMIT 50000
            """)
    List<DuplicateEquipmentDTO> findDuplicatePhysicalEquipments(@Param("taskId") Long taskId);
    @Query(nativeQuery = true, value = """
            select filename,
                   line_nb as lineNb,
                   datacenter_name as parentEquipmentName,
                   physical_equipment_name as equipmentName
            from check_inv_load_physical_equipment cilpe
            where cilpe.datacenter_name is null
            and cilpe.type IN ('Dedicated Server', 'Shared Server')
            and cilpe.task_id = :taskId
                        
            UNION
                        
            select filename,
                   line_nb as lineNb,
                   datacenter_name as parentEquipmentName,
                   physical_equipment_name as equipmentName
            from check_inv_load_physical_equipment cilpe
            where  not exists (
                select datacenter_name
                from in_datacenter idc
                where
                idc.digital_service_uid = :digitalServiceUid
               
                and cilpe.datacenter_name =  idc.name
            )
            and not exists (
                select datacenter_name
                from check_inv_load_datacenter cild
                where cild.task_id = cilpe.task_id
                and cilpe.datacenter_name = cild.datacenter_name
                and cild.datacenter_name not in (:parentDuplicates)
            )
            and cilpe.datacenter_name is not null
            and cilpe.type IN ('Dedicated Server', 'Shared Server')
            and cilpe.task_id = :taskId
                        
            """)
    List<CoherenceParentDTO> findIncoherentPhysicalEquipments(@Param("taskId") Long taskId,
                                                             @Param("digitalServiceUid") String digitalServiceUid,
                                                             @Param("parentDuplicates") List<String> parentDuplicates);

    @Query(nativeQuery = true, value = """
             select filename,
                    line_nb as lineNb,
                    datacenter_name as parentEquipmentName,
                    physical_equipment_name as equipmentName
             from check_inv_load_physical_equipment cilpe
             where cilpe.datacenter_name is null
             and cilpe.type IN ('Dedicated Server', 'Shared Server')
             and cilpe.task_id = :taskId
             
             UNION
             
             select filename,
                    line_nb as lineNb,
                    datacenter_name as parentEquipmentName,
                    physical_equipment_name as equipmentName
             from check_inv_load_physical_equipment cilpe
             where not exists (
                 select datacenter_name
                 from in_datacenter idc
                 where
                 idc.digital_service_uid = :digitalServiceUid
                
                 and cilpe.datacenter_name = idc.name
             )
             and not exists (
                 select datacenter_name
                 from check_inv_load_datacenter cild
                 where cild.task_id = cilpe.task_id
                 and cilpe.datacenter_name = cild.datacenter_name
             )
             and cilpe.datacenter_name is not null
             and cilpe.type IN ('Dedicated Server', 'Shared Server')
             and cilpe.task_id = :taskId
             
            """)    List<CoherenceParentDTO> findIncoherentPhysicalEquipments(@Param("taskId") Long taskId,
                                                                             @Param("digitalServiceUid") String digitalServiceUid);
}

