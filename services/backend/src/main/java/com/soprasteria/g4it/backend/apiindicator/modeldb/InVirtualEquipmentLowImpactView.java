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
                WITH vm_base AS (
                    SELECT
                        ove.name,
                        ove.task_id,
                        ove.lifecycle_step,
                        MAX(ove.quantity) AS quantity,
                        AVG(ove.unit_impact) AS avg_impact
                    FROM out_virtual_equipment ove
                    GROUP BY
                        ove.name,
                        ove.task_id,
                        ove.lifecycle_step
                )
                
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY
                            vm_base.name,
                            vm_base.lifecycle_step
                    ) AS id,
                
                    vm_base.name                                   AS name,
                    COALESCE(dc.location, ive.location, 'Unknown') AS country,
                    vm_base.lifecycle_step                         AS lifecycle_step,
                    oa.filters[1]                                  AS domain,
                    oa.filters[2]                                  AS sub_domain,
                    oa.environment                                 AS environment,
                    oa.equipment_type                              AS equipment_type,
                
                    MAX(vm_base.quantity)                          AS quantity,
                
                    CASE
                        WHEN AVG(vm_base.avg_impact) <= 0.1 THEN TRUE
                        ELSE FALSE
                    END                                            AS low_impact
                
                FROM vm_base
                
                JOIN task t
                    ON t.id = vm_base.task_id
                   AND t.status = 'COMPLETED'
                
                JOIN out_application oa
                    ON oa.task_id = vm_base.task_id
                   AND oa.virtual_equipment_name = vm_base.name
                
                JOIN in_virtual_equipment ive
                    ON ive.name = vm_base.name
                
                LEFT JOIN in_datacenter dc
                    ON dc.inventory_id = ive.inventory_id
                   AND dc.name = ive.datacenter_name
                
                WHERE ive.inventory_id = :inventoryId
                
                GROUP BY
                    vm_base.name,
                    COALESCE(dc.location, ive.location, 'Unknown'),
                    vm_base.lifecycle_step,
                    oa.filters[1],
                    oa.filters[2],
                    oa.environment,
                    oa.equipment_type;
                
                """,
        resultClass = InVirtualEquipmentLowImpactView.class
)
@Table(name = "in_virtual_equipment_low_impact_view")
public class InVirtualEquipmentLowImpactView {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "country")
    private String country;

    @Column(name = "lifecycle_step")
    private String lifecycleStep;

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

    @Column(name = "low_impact")
    private Boolean lowImpact;
}

