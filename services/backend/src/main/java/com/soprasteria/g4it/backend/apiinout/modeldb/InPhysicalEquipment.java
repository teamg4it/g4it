package com.soprasteria.g4it.backend.apiinout.modeldb;/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "in_physical_equipment")
public class InPhysicalEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_physical_equipment_id_seq")
    @SequenceGenerator(name = "in_physical_equipment_id_seq", sequenceName = "in_physical_equipment_id_seq", allocationSize = 100)
    private Long id;

    @NotNull
    private String name;

    /**
     * Linked Inventory
     */
    private Long inventoryId;

    /**
     * Linked Digital Service. Temporary field
     */
    private String digitalServiceUid;

    private String datacenterName;
    private String location;

    @NotNull
    @Min(0)
    private Double quantity;

    @Column(name = "nb_user")
    private Double numberOfUsers;

    private String type;
    private String model;
    private String manufacturer;

    private LocalDate datePurchase;
    private LocalDate dateWithdrawal;

    private String cpuType;
    @Min(0)
    private Double cpuCoreNumber;
    @Min(0)
    private Double sizeDiskGb;
    @Min(0)
    private Double sizeMemoryGb;

    private String source;
    private String quality;
    private String description;

    @Min(0)
    private Double electricityConsumption;

    @Min(0)
    @Max(8760)
    private Double durationHour;

    private List<String> commonFilters;

    private List<String> filters;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;
}
