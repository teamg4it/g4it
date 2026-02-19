package com.soprasteria.g4it.backend.apirecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an instantiated_recommendation.
 */
// il y a tous les attributs pour l'instant
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstantiatedRecommendationDTO {
    
    private Long idInstantiatedRecommendation;
    private Long idRecommendation;
    private Long idEvaluation;
    private Double priority;
    private String specificAffectedAttributes;
}
