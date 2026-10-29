/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.ecologits.client;

import com.soprasteria.g4it.backend.common.utils.JsonUtils;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import com.soprasteria.g4it.backend.external.ecologits.model.request.EcoEstimationRequestRest;
import com.soprasteria.g4it.backend.external.ecologits.model.response.EcoEstimationResponseRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client calling the EcoLogits calculation engine (EcoLogits API).
 */
@Slf4j
@Service
public class EcologitsClient {

    private static final String ESTIMATIONS_PATH = "/v1beta/estimations";

    @Autowired
    @Qualifier("webClientEcologits")
    WebClient webClient;

    /**
     * Get all available AI providers from Ecologits API
     *
     * @return the list of AI providers
     */
    public List<String> getAllAIProviders() {
        try {
            JsonNode response = webClient.get().uri("/v1beta/providers").retrieve().bodyToMono(JsonNode.class).block();
            return extractFieldValues(response, "providers", null);
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "ecologits-no-ai-provider-found");
        }
    }

    /**
     * Get all available models for one AI provider from Ecologits API
     *
     * @return the list of AI models
     */
    public List<String> getAllAIModels(String aiProvider) {
        try {
            JsonNode response = webClient.get().uri("/v1beta/models/" + aiProvider).retrieve().bodyToMono(JsonNode.class).block();
            return extractFieldValues(response, "models", "name");
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "ecologits-no-ai-provider-found");
        }
    }

    /**
     * Extract field values from a JSON array
     *
     * @param response  : the JSON response
     * @param arrayName : the name of the array from which the field is to be searched
     * @param fieldName : the name of the field from which the value is to be extracted
     * @return the list of values
     */
    private List<String> extractFieldValues(final JsonNode response, final String arrayName, final String fieldName) {
        // Getting the JSON array from the response if not null.
        JsonNode arrayValues = response == null ? null : response.get(arrayName);
        if (arrayValues == null || !arrayValues.isArray()) {
            throw new ExternalApiException(502, "ecologits-invalid-response");
        }

        // Set the list of values to return
        List<String> fields = new ArrayList<>(arrayValues.size());

        // Browse the array to look for field that matches fieldName
        for (JsonNode value : arrayValues) {
            // If the field is found, get its value
            JsonNode field = fieldName == null ? value : value.get(fieldName);
            if (field == null || !field.isString()) {
                throw new ExternalApiException(502, "ecologits-invalid-response");
            }
            // Add the field value to the list of values
            fields.add(field.stringValue());
        }
        return fields;
    }

    /**
     * Run the environmental impact estimation of an LLM usage on EcoLogits.
     *
     * @param request the estimation request (provider, model, output tokens, zone)
     * @return the estimation response
     */
    public EcoEstimationResponseRest runEstimation(final EcoEstimationRequestRest request) {

        String context = String.join("-", request.getProvider(), request.getModelName());

        try {
            EcoEstimationResponseRest response = webClient.post()
                    .uri(ESTIMATIONS_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request), EcoEstimationRequestRest.class)
                    .retrieve()
                    .bodyToMono(EcoEstimationResponseRest.class)
                    .block();

            if (response == null) {
                throw new ExternalApiException(500,
                        String.format("the response is null when calling ecologits with %s", context));
            }
            return response;
        } catch (WebClientRequestException e) {
            log.error("Error calling EcoLogits with body {}", JsonUtils.toJson(request));
            throw new ExternalApiException(400, String.format("Context: %s, error: %s", context, e.getMessage()));
        } catch (WebClientResponseException e) {
            log.error("Error calling EcoLogits with body {}", JsonUtils.toJson(request));
            throw new ExternalApiException(e.getStatusCode().value(), String.format("Context: %s, error: %s", context, e.getMessage()));
        }
    }
}