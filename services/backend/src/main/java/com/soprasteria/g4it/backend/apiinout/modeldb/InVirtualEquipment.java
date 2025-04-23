package com.soprasteria.g4it.backend.apiinout.modeldb;/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "in_virtual_equipment")
public class InVirtualEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_virtual_equipment_id_seq")
    @SequenceGenerator(name = "in_virtual_equipment_id_seq", sequenceName = "in_virtual_equipment_id_seq", allocationSize = 100)
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

    private String physicalEquipmentName;

    @NotNull
    @Min(0)
    private Double quantity;

    @NotNull
    private String infrastructureType;

    private String instanceType;

    private String type;

    private String provider;

    private String location;

    @Min(0)
    private Double durationHour;

    @Min(0)
    private Double workload;

    @Min(0)
    private Double electricityConsumption;

    @Min(0)
    private Double vcpuCoreNumber;

    @Min(0)
    private Double sizeMemoryGb;

    @Min(0)
    private Double sizeDiskGb;

    @Min(0)
    private Double allocationFactor;

    private List<String> commonFilters;

    private List<String> filters;

    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;

    /**
     * Used by csv-headers.yml
     */
    public String getCloudProvider() {
        return this.getProvider();
    }

    /**
     * Used by csv-headers.yml
     */
    public Double getAverageWorkload() {
        return this.getWorkload();
    }

    /**
     * Used by csv-headers.yml
     */
    public Double getUsageDuration() {
        return this.getDurationHour();
    }

}
