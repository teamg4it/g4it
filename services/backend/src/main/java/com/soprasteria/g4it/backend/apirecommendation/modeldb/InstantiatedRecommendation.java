package com.soprasteria.g4it.backend.apirecommendation.modeldb;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


/**
 * Entity representing the instantiated_recommendation table.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "instanciated_recommendation")
public class InstantiatedRecommendation{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instantiated_recommendation")
    private Long idInstantiatedRecommendation;

    @NotNull
    @Column(name = "id_recommendation")
    private Long idRecommendation;

    @NotNull
    @Column(name = "id_evaluation")
    private Long idEvaluation;

    @Column(name = "priority")
    private Double priority;

    @Column(name = "specific_affected_attributes", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String specificAffectedAttributes;


}