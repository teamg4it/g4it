package com.soprasteria.g4it.backend.apirecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a recommendation.
 */
// il y a tous les attributs pour l'instant
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {

    private Long idRecommendation;
    private String title;
    private String description;
    private String category;
    private String affectedAttributes;
    private String heuristicRange;
    private Integer baseImpact;
    private Long organisationId;
}
