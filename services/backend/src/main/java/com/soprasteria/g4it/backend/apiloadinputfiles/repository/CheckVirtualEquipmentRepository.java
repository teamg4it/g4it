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
import com.soprasteria.g4it.backend.apiloadinputfiles.modeldb.CheckVirtualEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CheckVirtualEquipment JPA repository.
 */
@Repository
public interface CheckVirtualEquipmentRepository extends JpaRepository<CheckVirtualEquipment, Long> {

    /**
     * Find virtual equipment metadata by the task id
     *
     * @param taskId task id
     * @return return the checkVirtualEquipments
     */
    List<CheckVirtualEquipment> findByTaskId(Long taskId);

   /**
    * Retrieve duplicate virtual equipment with details by task id.
    *
    * @param taskId the task id
    * @return List of DuplicateEquipmentDTO containing virtual equipment names and their file name and line number
   */
    @Query(nativeQuery = true, value = """
            SELECT ve.virtual_equipment_name as equipmentName,
                       STRING_AGG(ve.filename || ':' || ve.line_nb, ',') as filenameLineInfo
                FROM check_inv_load_virtual_equipment ve
                WHERE ve.task_id = :taskId
                GROUP BY ve.virtual_equipment_name
                HAVING COUNT(*) > 1
                LIMIT 50000
            """)
    List<DuplicateEquipmentDTO> findDuplicateVirtualEquipments(@Param("taskId") Long taskId);

    @Query(nativeQuery = true, value = """
            select filename,
                   line_nb as lineNb,
                   physical_equipment_name as parentEquipmentName,
                   virtual_equipment_name as equipmentName
            from check_inv_load_virtual_equipment cilve
            where cilve.infrastructure_type != 'CLOUD_SERVICES'
            and cilve.physical_equipment_name is null
            and cilve.task_id = :taskId
                        
            UNION
                        
            select filename,
                   line_nb as lineNb,
                   physical_equipment_name as parentEquipmentName,
                   virtual_equipment_name as equipmentName
            from check_inv_load_virtual_equipment cilve
            where cilve.infrastructure_type != 'CLOUD_SERVICES'
            and not exists (
                select physical_equipment_name
                from in_physical_equipment ipe
                where ipe.inventory_id = :inventoryId
                and cilve.physical_equipment_name = ipe.name
            )
            and not exists (
                select physical_equipment_name
                from check_inv_load_physical_equipment cilpe
                where cilpe.task_id = cilve.task_id
                and cilve.physical_equipment_name = cilpe.physical_equipment_name
                and cilpe.physical_equipment_name not in (:parentDuplicates)
            )
            and cilve.physical_equipment_name is not null
            and cilve.task_id = :taskId
                        
            """)
    List<CoherenceParentDTO> findIncoherentVirtualEquipments(@Param("taskId") Long taskId, @Param("inventoryId") Long inventoryId, @Param("parentDuplicates") List<String> parentDuplicates);

    @Query(nativeQuery = true, value = """
             select filename,
                    line_nb as lineNb,
                    physical_equipment_name as parentEquipmentName,
                    virtual_equipment_name as equipmentName
             from check_inv_load_virtual_equipment cilve
             where cilve.infrastructure_type != 'CLOUD_SERVICES'
             and cilve.physical_equipment_name is null
             and cilve.task_id = :taskId
             
             UNION
             
             select filename,
                    line_nb as lineNb,
                    physical_equipment_name as parentEquipmentName,
                    virtual_equipment_name as equipmentName
             from check_inv_load_virtual_equipment cilve
             where cilve.infrastructure_type != 'CLOUD_SERVICES'
             and not exists (
                 select physical_equipment_name
                 from in_physical_equipment ipe
                 where ipe.inventory_id = :inventoryId
                 and cilve.physical_equipment_name = ipe.name
             )
             and not exists (
                 select physical_equipment_name
                 from check_inv_load_physical_equipment cilpe
                 where cilpe.task_id = cilve.task_id
                 and cilve.physical_equipment_name = cilpe.physical_equipment_name
             )
             and cilve.physical_equipment_name is not null
             and cilve.task_id = :taskId
             
            """)    List<CoherenceParentDTO> findIncoherentVirtualEquipments(@Param("taskId") Long taskId, @Param("inventoryId") Long inventoryId);

}
