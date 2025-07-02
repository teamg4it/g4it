/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.ecomindai.client;

import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.InputEstimationLLMInference;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.LLMModelConfig;
import com.soprasteria.g4it.backend.client.gen.connector.apiecomindv2.dto.OutputEstimation;
import com.soprasteria.g4it.backend.external.ecomindai.model.AIConfigurationBO;
import com.soprasteria.g4it.backend.exception.ExternalApiException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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
    public List<LLMModelConfig> getAiModelConfig() {
        try {
            List<LLMModelConfig> response = webModelAiConfigapi.get().uri("/llm_configurations").retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<LLMModelConfig>>() {}).block();
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
     * @param inputEstimationLLMInference the input estimation for LLM inference
     * @return the response
     */
    public OutputEstimation runEstimation(InputEstimationLLMInference inputEstimationLLMInference) {

        try {
            OutputEstimation response = webModelAiEstimationapi
                    .post()
                    .uri("/estimate_llm_inference")
                    .bodyValue(inputEstimationLLMInference)
                    .retrieve()
                    .bodyToMono(OutputEstimation.class)
                    .block();
            if (response == null) {
                throw new ExternalApiException(404, "ai-model-no-estimate-found");
            }
            return response;
        } catch (WebClientRequestException e) {
            throw new ExternalApiException(404, "ai-model-no-estimate-found");
        }
    }
}
