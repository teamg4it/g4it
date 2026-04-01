/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apirecommendationds.mapper;

import com.soprasteria.g4it.backend.apirecommendationds.modeldb.InstantiatedRecommendation;
import com.soprasteria.g4it.backend.apirecommendationds.modeldb.Recommendation;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InstantiatedRecommendationRest;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Recommendation and InstantiatedRecommendation.
 */
@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface RecommendationMapper {


    /**
     * Map Recommendation entity to RecommendationDSRest DTO.
     */
    RecommendationDSRest toRest(Recommendation recommendation);

    /**
     * Map list of Recommendation entities to list of RecommendationDSRest DTOs.
     */
    List<RecommendationDSRest> toRestList(List<Recommendation> recommendations);

    /**
     * Map RecommendationDSRest DTO to Recommendation entity.
     * idRecommendation is ignored on creation (auto-generated).
     */
    @Mapping(target = "idRecommendation", ignore = true)
    Recommendation toEntity(RecommendationDSRest recommendationDSRest);

    /**
     * Merge updated fields into existing Recommendation entity.
     * idRecommendation and organisationId are never overwritten.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idRecommendation", ignore = true)
    @Mapping(target = "organisationId", ignore = true)
    void merge(@MappingTarget Recommendation target, Recommendation source);


    /**
     * Map InstantiatedRecommendation entity to InstantiatedRecommendationRest DTO.
     * The nested recommendation field is set manually in the service.
     */
    @Mapping(target = "recommendation", ignore = true)
    @Mapping(target = "digitalServiceVersionUid", ignore = true)
    InstantiatedRecommendationRest toInstantiatedRest(InstantiatedRecommendation instantiatedRecommendation);

    /**
     * Map list of InstantiatedRecommendation entities to list of DTOs.
     */
    List<InstantiatedRecommendationRest> toInstantiatedRestList(List<InstantiatedRecommendation> instantiatedRecommendations);
}
