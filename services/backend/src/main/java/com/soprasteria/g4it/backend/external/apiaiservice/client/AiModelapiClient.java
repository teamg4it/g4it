/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.apiaiservice.client;

import com.soprasteria.g4it.backend.external.apiaiservice.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import java.util.List;

@Slf4j
@Service
public class AiModelapiClient {

    @Autowired
    @Qualifier("webModelAiConfigapi")
    WebClient webModelAiConfigapi;

    @Autowired
    @Qualifier("webModelAiEstimationapi")
    WebClient webModelAiEstimationapi;

    /**
     * Get all config of AI model from Ecomind
     *
     * @return the response
     */
    public String getAiModelConfig(String type) {
        try {
            String response = webModelAiConfigapi.get().uri("/data").retrieve()
                    .bodyToMono(String.class).block();
            if (response == null) {
                throw new ExternalApiException(404, "ai-model-no-config-found");
            }
            return response;
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "ai-model-no-config-found");
        }
    }
    /**
     * Run AI Model Estimation on Ecomind
     *
     * @param type of the AI model confiuration
     * @param stage of the AI model confiuration
     * @param aiConfigurationBO of the AI model confiuration
     * @return the response
     */
    public String runEstimation(String type, String stage, List<@Valid AIConfigurationBO> aiConfigurationBO) {

        try {
            String response = webModelAiEstimationapi.get().uri("/data").retrieve()
                    .bodyToMono(String.class).block();
            if (response == null) {
                throw new ExternalApiException(404, "ai-model-no-estimate-found");
            }
            return response;
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "ai-model-no-estimate-found");
        }
    }
}
