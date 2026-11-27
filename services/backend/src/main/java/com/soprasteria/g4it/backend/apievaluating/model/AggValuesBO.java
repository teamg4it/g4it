/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@SuperBuilder
@Data
public class AggValuesBO {
    private Long countValue;
    private Double unitImpact;
    private Double peopleEqImpact;
    private Double electricityConsumption;
    private double quantityWeightedElectricityConsumption;
    private boolean electricityAllocationApplied;
    private Double quantity;
    private Double lifespan;
    private Double usageDuration;
    private Double workload;
    private Set<String> errors;
    private String source;

    public AggValuesBO() {
        this.countValue = 0L;
        this.unitImpact = 0d;
        this.peopleEqImpact = 0d;
        this.electricityConsumption = 0d;
        this.quantity = 0d;
        this.lifespan = 0d;
        this.usageDuration = 0d;
        this.workload = 0d;
        this.errors = new HashSet<>();
        this.source = null;
    }

    public void add(AggValuesBO v) {
        this.countValue += v.getCountValue();
        this.unitImpact += v.getUnitImpact();
        this.peopleEqImpact += v.getPeopleEqImpact();
        this.electricityConsumption += v.getElectricityConsumption();
        this.quantityWeightedElectricityConsumption += v.getElectricityConsumption() * v.getQuantity();
        this.electricityAllocationApplied |= v.isElectricityAllocationApplied();
        this.quantity += v.getQuantity();
        this.lifespan += v.getLifespan();
        this.usageDuration += v.getUsageDuration() * v.getQuantity();
        this.workload += v.getWorkload() * v.getQuantity();
        if (!v.getErrors().isEmpty()) {
            this.errors.addAll(v.getErrors());
        }
        if (this.source == null) {
            this.source = v.getSource();
        }
    }

    /**
     * Preserve the existing indicator contract (consumption * quantity / count).
     * For an allocation-affected physical group, normalize the sum of each source's
     * per-unit consumption times its already allocated quantity to that contract.
     * Unallocated groups retain their historical, unweighted consumption sum.
     */
    public Double getPhysicalElectricityConsumption() {
        if (!electricityAllocationApplied) {
            return electricityConsumption;
        }
        return quantity == 0d ? 0d : quantityWeightedElectricityConsumption * countValue / quantity;
    }

}