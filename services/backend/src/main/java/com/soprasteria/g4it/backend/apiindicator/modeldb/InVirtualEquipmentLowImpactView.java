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
@IdClass(VirtualEquipmentLowImpactId.class)
@NamedNativeQuery(
        name = "InVirtualEquipmentLowImpactView.findVirtualEquipmentLowImpactIndicatorsByInventoryId",
        query = """
                SELECT
                       ive.inventory_id                         AS inventory_id,
                       inv.name                                 AS inventory_name,
                       COALESCE(dc.location, ive.location, 'Unknown') AS country,                
                       ive.infrastructure_type                  AS infrastructure_type,
                       ive.provider                             AS provider,
                       ive.common_filters[1]                    AS nom_entite,
                       oa.lifecycle_step                        AS lifecycle_step,
                       oa.filters[1]                            AS domain,
                       oa.filters[2]                            AS sub_domain,
                       oa.environment                           AS environment,
                       oa.equipment_type                        AS equipment_type,
                       SUM(ove.quantity)                        AS quantity,
                       AVG(ove.unit_impact) <= 0.1              AS low_impact
                   FROM in_virtual_equipment ive
                   JOIN inventory inv
                       ON inv.id = ive.inventory_id
                   JOIN out_virtual_equipment ove
                       ON ove.name = ive.name
                   JOIN task t
                       ON t.id = ove.task_id
                      AND t.status = 'COMPLETED'
                   JOIN out_application oa
                       ON oa.task_id = t.id
                   LEFT JOIN in_datacenter dc
                       ON dc.inventory_id = ive.inventory_id
                      AND dc.name = ive.datacenter_name
                   WHERE ive.inventory_id = :inventoryId
                   GROUP BY
                       ive.inventory_id,
                       inv.name,
                       dc.location,
                       ive.location,
                       ive.infrastructure_type,
                       ive.provider,
                       ive.common_filters,
                       oa.lifecycle_step,
                       oa.filters,
                       oa.environment,
                       oa.equipment_type;
                
                """,
        resultClass = InVirtualEquipmentLowImpactView.class
)
@Table(name = "in_virtual_equipment_low_impact_view")
public class InVirtualEquipmentLowImpactView {

    @Id
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "inventory_name")
    private String inventoryName;

    @Column(name = "country")
    private String country;

    @Column(name = "infrastructure_type")
    private String infrastructureType;

    @Column(name = "provider")
    private String provider;

    @Column(name = "nom_entite")
    private String nomEntite;

    @Id
    @Column(name = "lifecycle_step")
    private String lifecycleStep;

    @Column(name = "domain")
    private String domain;

    @Column(name = "sub_domain")
    private String subDomain;

    @Id
    @Column(name = "environment")
    private String environment;

    @Column(name = "equipment_type")
    private String equipmentType;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "low_impact")
    private Boolean lowImpact;
}

