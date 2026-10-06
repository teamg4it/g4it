/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.ecologits;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for the EcoLogits calculation engine.
 */
@Configuration
public class ConfigEcologits {

    @Value("${ecologits.base-url}")
    private String ecologitsBaseUrl;

    @Bean
    public WebClient webClientEcologits() {
        return WebClient.builder().baseUrl(ecologitsBaseUrl).build();
    }

}