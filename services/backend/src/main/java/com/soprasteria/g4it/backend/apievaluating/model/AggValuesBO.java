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

}