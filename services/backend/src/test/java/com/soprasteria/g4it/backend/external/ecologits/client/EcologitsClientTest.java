/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.ecologits.client;

import com.soprasteria.g4it.backend.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EcologitsClientTest {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @InjectMocks
    private EcologitsClient client;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(client, "webClient", webClient);
    }

    @Test
    void getAllAIProviders_shouldExtractProviderNamesFromResponseObject() throws Exception {
        JsonNode response = JSON_MAPPER.readTree("""
                {"providers":["anthropic","openai"]}
                """);
        mockGetResponse("/v1beta/providers", response);

        assertEquals(List.of("anthropic", "openai"), client.getAllAIProviders());
    }

    @Test
    void getAllAIModels_shouldExtractModelNamesFromResponseObjects() throws Exception {
        JsonNode response = JSON_MAPPER.readTree("""
                {"models":[{"provider":"anthropic","name":"claude-sonnet"},{"provider":"anthropic","name":"claude-haiku"}]}
                """);
        mockGetResponse("/v1beta/models/anthropic", response);

        assertEquals(List.of("claude-sonnet", "claude-haiku"), client.getAllAIModels("anthropic"));
    }

    @Test
    void getAllAIProviders_shouldRejectUnexpectedResponseShape() throws Exception {
        mockGetResponse("/v1beta/providers", JSON_MAPPER.readTree("[\"anthropic\"]"));

        assertThrows(ExternalApiException.class, client::getAllAIProviders);
    }

    private void mockGetResponse(final String path, final JsonNode response) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(path)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(response));
    }
}
