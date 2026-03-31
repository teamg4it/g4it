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
import java.util.List;

/**
 * Entity representing the recommendation table.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "recommendation")
public class Recommendation{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recommendation")
    private Long idRecommendation;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "category", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> category;

    @Column(name = "affected_attributes", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String affectedAttributes;

    @Column(name = "heuristic_range", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String heuristicRange;

    @Column(name = "base_impact")
    private Integer baseImpact;

    @Column(name = "organisation_id")
    private Long organisationId;

}








