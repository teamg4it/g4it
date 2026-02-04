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
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Immutable
@Table(name = "number_of_virtual_equipments_view")
@NamedNativeQuery(
        name = "NumberOfVirtualEquipmentsView.findByInventoryId",
        query = """
                SELECT
                    ive.inventory_id            AS inventory_id,
                    inv.name                    AS inventory_name,
                    COALESCE(SUM(ive.quantity), 0) AS number_of_virtual_equipments
                FROM in_virtual_equipment ive
                JOIN inventory inv
                    ON inv.id = ive.inventory_id
                WHERE ive.inventory_id = :inventoryId
                GROUP BY ive.inventory_id, inv.name
                """,
        resultClass = NumberOfVirtualEquipmentsView.class
)
public class NumberOfVirtualEquipmentsView {

    @Id
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "inventory_name")
    private String inventoryName;

    @Column(name = "number_of_virtual_equipments")
    private Long numberOfVirtualEquipments;
}


