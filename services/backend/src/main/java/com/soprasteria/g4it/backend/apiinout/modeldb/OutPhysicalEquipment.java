/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiinout.modeldb;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "out_physical_equipment")
public class OutPhysicalEquipment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "out_physical_equipment_id_seq")
    @SequenceGenerator(name = "out_physical_equipment_id_seq", sequenceName = "out_physical_equipment_id_seq", allocationSize = 100)
    private Long id;

    private String name;

    private Long taskId;

    private String criterion;

    private String lifecycleStep;

    private String location;

    private String equipmentType;

    private String engineName;

    private String engineVersion;

    private String referentialVersion;

    private String statusIndicator;

    @Min(0)
    private Double quantity;

    @Min(0)
    private Double unitImpact;

    @Min(0)
    private Double peopleEqImpact;

    @Min(0)
    private Double electricityConsumption;

    @Min(0)
    private Double lifespan;

    @Min(0)
    private Long countValue;

    @Min(0)
    @Column(name = "nb_user")
    private Double numberOfUsers;

    private String unit;
    private String reference;
    private String hostingEfficiency;

    private List<String> commonFilters;

    private List<String> filters;

    private Set<String> errors;

    private String source;

}
