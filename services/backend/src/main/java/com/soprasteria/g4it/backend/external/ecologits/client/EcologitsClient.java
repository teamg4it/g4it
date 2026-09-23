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

/**
 * HTTP client calling the EcoLogits calculation engine (EcoLogits API).
 */
@Slf4j
@Service
public class EcologitsClient {

    public static final String ECOLOGITS_ENGINE = "EcoLogits";

    private static final String ESTIMATIONS_PATH = "/v1beta/estimations";

    @Autowired
    @Qualifier("webClientEcologits")
    WebClient webClient;

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