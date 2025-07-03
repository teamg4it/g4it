/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apirecomandation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.Recommendation;
import com.soprasteria.g4it.backend.external.ecomindai.model.RecommendationBO;

import java.util.List;

public interface RecommendationJsonMapper {
    ObjectMapper MAPPER = new ObjectMapper();

    static String toJson(List<Recommendation> list) {
        try {
            return MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static List<Recommendation> fromJson(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}