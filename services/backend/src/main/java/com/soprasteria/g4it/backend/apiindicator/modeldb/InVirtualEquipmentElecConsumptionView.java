/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.modeldb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@SqlResultSetMapping(
        name = "InVirtualEquipmentElecConsumptionIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = InVirtualEquipmentElecConsumptionView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "name"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "lifecycle_step"),
                        @ColumnResult(name = "domain"),
                        @ColumnResult(name = "sub_domain"),
                        @ColumnResult(name = "environment"),
                        @ColumnResult(name = "equipment_type"),
                        @ColumnResult(name = "elec_consumption", type = Double.class)
                }
        )
)

@NamedNativeQuery(
        name = "InVirtualEquipmentElecConsumptionView.findVirtualEquipmentElecConsumptionIndicators",
        resultSetMapping = "InVirtualEquipmentElecConsumptionIndicatorsMapping",
        query = """
                SELECT
                     ROW_NUMBER() OVER () AS id,
                
                     oa.virtual_equipment_name AS name,
                
                     COALESCE(dc.location, ive.location, 'Unknown') AS country,
                
                     'USING' AS lifecycle_step,
                
                     oa.filters[1] AS domain,
                     oa.filters[2] AS sub_domain,
                     oa.environment,
                     oa.equipment_type,
                
                     SUM(
                         (
                             (oa.electricity_consumption * ive.quantity)
                             / NULLIF(oa.count_value, 0)
                         ) / :criteriaNumber
                     ) AS elec_consumption
                
                 FROM out_application oa
                
                 JOIN task t
                     ON t.id = oa.task_id
                    AND t.status = 'COMPLETED'
                    AND t.id = :taskId
                
                 JOIN in_virtual_equipment ive
                     ON ive.name = oa.virtual_equipment_name
                     AND ive.inventory_id = t.inventory_id
                
                 LEFT JOIN in_datacenter dc
                     ON dc.name = ive.datacenter_name
                    AND dc.inventory_id = ive.inventory_id
                
                 WHERE oa.lifecycle_step = 'USING'
                
                 GROUP BY
                     oa.virtual_equipment_name,
                     COALESCE(dc.location, ive.location, 'Unknown'),
                     oa.filters[1],
                     oa.filters[2],
                     oa.environment,
                     oa.equipment_type
                """
)

@Data
@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
public class InVirtualEquipmentElecConsumptionView implements Serializable {

    @Id
    private Long id;

    private String name;

    private String country;

    @Column(name = "lifecycle_step")
    private String lifecycleStep;

    private String domain;

    @Column(name = "sub_domain")
    private String subDomain;

    private String environment;

    @Column(name = "equipment_type")
    private String equipmentType;

    @Column(name = "elec_consumption")
    private Double elecConsumption;
}