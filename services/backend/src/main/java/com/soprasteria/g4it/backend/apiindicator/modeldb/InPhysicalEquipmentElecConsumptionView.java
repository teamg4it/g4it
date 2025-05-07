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
        name = "InPhysicalEquipmentElecConsumptionIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = InPhysicalEquipmentElecConsumptionView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "elec_consumption", type = Double.class)
                }
        )
)

@NamedNativeQuery(name = "InPhysicalEquipmentElecConsumptionView.findPhysicalEquipmentElecConsumptionIndicators",
        resultSetMapping = "InPhysicalEquipmentElecConsumptionIndicatorsMapping", query = """
        SELECT
        ROW_NUMBER() OVER ()      AS id,
        "location"                AS country,
        equipment_type            AS type,
        common_filters[1]         AS nom_entite,
        filters[1]                AS statut ,
        sum(ope.electricity_consumption) / :criteriaNumber * ope.quantity as elec_consumption
        FROM out_physical_equipment ope
        WHERE lifecycle_step = 'USING'
        AND task_id = :taskId
        GROUP BY
          "location" ,
          equipment_type,
          common_filters,
          filters,
          quantity;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class InPhysicalEquipmentElecConsumptionView implements Serializable {

    @Id
    private Long id;

    private String nomEntite;

    private String type;

    private String statut;

    private String country;

    private Double elecConsumption;
}
