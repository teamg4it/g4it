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
        name = "InPhysicalEquipmentAvgAgeIndicatorsMapping",
        classes = @ConstructorResult(
                targetClass = InPhysicalEquipmentAvgAgeView.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "country"),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "nom_entite"),
                        @ColumnResult(name = "statut"),
                        @ColumnResult(name = "poids", type = Double.class),
                        @ColumnResult(name = "age_moyen", type = Double.class)
                }
        )
)

@NamedNativeQuery(name = "InPhysicalEquipmentAvgAgeView.findPhysicalEquipmentAvgAgeIndicators",
        resultSetMapping = "InPhysicalEquipmentAvgAgeIndicatorsMapping", query = """
        WITH equipment_lifespan AS (
                            SELECT
                                reference,
                                location,
                                equipment_type,
                                common_filters,
                                filters,
                                MAX(quantity) AS quantity,
                                MAX(lifespan) AS lifespan
                            FROM out_physical_equipment
                            WHERE task_id = :taskId
                              AND status_indicator = 'OK'
                              AND lifespan IS NOT NULL
                              AND lifespan > 0
                            GROUP BY
                                reference,
                                location,
                                equipment_type,
                                common_filters,
                                filters
                        )
                        SELECT
                            ROW_NUMBER() OVER ()          AS id,
                            location                      AS country,
                            equipment_type                AS type,
                            common_filters[1]             AS nom_entite,
                            filters[1]                    AS statut,
                            SUM(quantity)                 AS poids,
                            SUM(lifespan) / SUM(quantity) AS age_moyen
                        FROM equipment_lifespan
                        GROUP BY
                            location,
                            equipment_type,
                            common_filters,
                            filters;
        """)
@Data
@Entity
@SuperBuilder
@AllArgsConstructor
public class InPhysicalEquipmentAvgAgeView implements Serializable {

    @Id
    private Long id;

    private String country;

    private String type;

    private String nomEntite;

    private String statut;

    private Double poids;

    private Double ageMoyen;
}
