/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.modeldb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Data
@NoArgsConstructor
@Entity
@Immutable
@NamedNativeQuery(
        name = "InVirtualEquipmentCountView.findVirtualEquipmentCountIndicatorsByInventoryId",
        query = """
                WITH latest_task AS (
                                                            SELECT id
                                                            FROM task
                                                            WHERE inventory_id = :inventoryId
                                                              AND status = 'COMPLETED'
                                                            ORDER BY id DESC
                                                            LIMIT 1
                                                        )
                
                                                        SELECT
                                                            ROW_NUMBER() OVER () AS id,
                                                            ive.name AS name,
                                                            ive.quantity AS quantity,
                
                                                            COALESCE(dc.location, ive.location) AS location,
                
                                                            oa.environment,
                                                            oa.lifecycle_step,
                                                            oa.filters[1] AS domain,
                                                            oa.filters[2] AS sub_domain,
                                                            oa.equipment_type
                
                                                        FROM latest_task lt
                
                                                        JOIN out_application oa
                                                            ON oa.task_id = lt.id
                                                           AND oa.status_indicator = 'OK'
                
                                                        JOIN in_virtual_equipment ive
                                                            ON ive.name = oa.virtual_equipment_name
                                                           AND ive.inventory_id = :inventoryId
                
                                                        LEFT JOIN in_datacenter dc
                                                            ON dc.name = ive.datacenter_name
                                                           AND dc.inventory_id = ive.inventory_id
                
                                                        GROUP BY
                                                            ive.name,
                                                            ive.infrastructure_type,
                                                            ive.quantity,
                                                            COALESCE(dc.location, ive.location),
                                                            oa.environment,
                                                            oa.lifecycle_step,
                                                            oa.filters[1],
                                                            oa.filters[2],
                                                            oa.equipment_type
                """,
        resultClass = InVirtualEquipmentCountView.class
)
public class InVirtualEquipmentCountView {
    @Id
    private Long id;

    private String name;

    private String location;

    @Column(name = "lifecycle_step")
    private String lifeCycle;

    private String domain;

    @Column(name = "sub_domain")
    private String subDomain;

    private String environment;

    @Column(name = "equipment_type")
    private String equipmentType;

    private Integer quantity;
}
