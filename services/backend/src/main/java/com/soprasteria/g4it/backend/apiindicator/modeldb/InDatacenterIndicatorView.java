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
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SqlResultSetMapping(
        name = "InDatacenterIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = InDatacenterIndicatorView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "name"),
                        @ColumnResult(name = "location"),
                        @ColumnResult(name = "pue", type = Double.class),
                        @ColumnResult(name = "physical_equipment_count", type = Double.class),
                        @ColumnResult(name = "entity"),
                        @ColumnResult(name = "status"),
                        @ColumnResult(name = "equipment")

                }
        )
)
@NamedNativeQuery(name = "InDatacenterIndicatorView.findDataCenterIndicators", resultSetMapping = "InDatacenterIndicatorsMapping", query = """
            SELECT Row_number()
                   OVER ()           AS id,
                   dc."name"         AS NAME,
                   dc."location"     AS location,
                   dc.pue            AS pue,
                   Sum(ipe.quantity) AS physical_equipment_count,
                   ipe.common_filters[1]       AS entity,
                   ipe.filters[1]       AS status,
                   ipe.type    AS equipment
            FROM   in_datacenter dc
                   LEFT JOIN in_physical_equipment ipe
                           ON dc.inventory_id = ipe.inventory_id
                              AND dc."name" = ipe.datacenter_name
            WHERE  dc.inventory_id = :inventoryId
            GROUP  BY dc."name",
                      dc."location",
                      dc.pue,
                      ipe.common_filters[1],
                      ipe.filters[1],
                      ipe.type
        """)

@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class InDatacenterIndicatorView implements Serializable {

    @Id
    private Long id;

    private String name;

    private String location;

    private Double pue;

    private Double physicalEquipmentCount;

    private String entity;

    private String status;

    private String equipment;



}
