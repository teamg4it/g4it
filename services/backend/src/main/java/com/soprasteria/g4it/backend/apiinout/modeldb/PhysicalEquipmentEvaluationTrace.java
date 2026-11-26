// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

package com.soprasteria.g4it.backend.apiinout.modeldb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "physical_equipment_evaluation_trace",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "source_equipment_id"}))
public class PhysicalEquipmentEvaluationTrace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "source_equipment_id", nullable = false)
    private Long sourceEquipmentId;

    @Column(name = "item_type", columnDefinition = "text")
    private String itemType;

    @Column(name = "item_category", nullable = false, columnDefinition = "text")
    private String itemCategory;

    @Column(columnDefinition = "text")
    private String reference;

    @Column(name = "usage_context", columnDefinition = "text")
    private String usageContext;

    @Column(name = "usage_fraction", columnDefinition = "text")
    private String usageFraction;

    @Column(name = "effective_fraction", nullable = false)
    private Double effectiveFraction;

    @Column(name = "original_quantity")
    private Double originalQuantity;
}
