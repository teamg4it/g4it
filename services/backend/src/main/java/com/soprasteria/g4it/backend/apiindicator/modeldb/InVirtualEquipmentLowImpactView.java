/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.modeldb;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Data
@NoArgsConstructor
@Entity
@Immutable
@NamedNativeQuery(
        name = "InVirtualEquipmentLowImpactView.findVirtualEquipmentLowImpactIndicatorsByInventoryId",
        query = """
                SELECT
                                       ROW_NUMBER() OVER (
                                           ORDER BY
                                               oa.virtual_equipment_name,
                                               oa.lifecycle_step,
                                               oa.environment
                                       ) AS id,
                
                                       oa.virtual_equipment_name                     AS name,
                                       COALESCE(dc.location, ive.location, 'Unknown') AS location,
                                       oa.lifecycle_step                             AS lifecycle_step,
                                       oa.filters[1]                                 AS domain,
                                       oa.filters[2]                                 AS sub_domain,
                                       oa.environment                                AS environment,
                                       oa.equipment_type                             AS equipment_type,
                
                                       COUNT(DISTINCT oa.name)                                      AS quantity
                
                                   FROM out_application oa
                
                                   JOIN in_virtual_equipment ive
                                         ON ive.name = oa.virtual_equipment_name
                                        AND ive.inventory_id = :inventoryId
                
                                   LEFT JOIN in_datacenter dc
                                         ON dc.inventory_id = ive.inventory_id
                                        AND dc.name = ive.datacenter_name
                
                                   WHERE oa.task_id = (
                                           SELECT MAX(t2.id)
                                           FROM task t2
                                           WHERE t2.inventory_id = :inventoryId
                                             AND t2.status = 'COMPLETED'
                                         )
                                     AND oa.status_indicator = 'OK'
                
                                   GROUP BY
                                       oa.virtual_equipment_name,
                                       COALESCE(dc.location, ive.location, 'Unknown'),
                                       oa.lifecycle_step,
                                       oa.filters[1],
                                       oa.filters[2],
                                       oa.environment,
                                       oa.equipment_type
                """,
        resultClass = InVirtualEquipmentLowImpactView.class
)
public class InVirtualEquipmentLowImpactView {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "lifecycle_step")
    private String lifeCycle;

    @Column(name = "domain")
    private String domain;

    @Column(name = "sub_domain")
    private String subDomain;

    @Column(name = "environment")
    private String environment;

    @Column(name = "equipment_type")
    private String equipmentType;

    @Column(name = "quantity")
    private Double quantity;

    @Transient
    private Boolean lowImpact;
}
