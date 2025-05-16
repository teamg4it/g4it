package com.soprasteria.g4it.backend.apiaiservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ConfigAiModel {
    @Value("${aimodelconfigapi.base-url}")
    private String aimodelconfigurationapiBaseUrl;

    @Value("${aimodelestimationapi.base-url}")
    private String aimodelestimationapiBaseUrl;

    @Bean
    public WebClient webModelAiConfigapi() {
        return WebClient.builder().baseUrl(aimodelconfigurationapiBaseUrl).build();
    }
    @Bean
    public WebClient webModelAiEstimationapi() {
        return WebClient.builder().baseUrl(aimodelestimationapiBaseUrl).build();
    }
}
