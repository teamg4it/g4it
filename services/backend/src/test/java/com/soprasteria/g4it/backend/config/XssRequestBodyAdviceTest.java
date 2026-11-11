/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that {@link XssRequestBodyAdvice} blocks malicious payloads for ANY
 * {@code @RequestBody}, reproducing the real-world exploit reported against
 * POST /organizations/{organization}/workspaces/{workspace}/digital-service-version.
 */
class XssRequestBodyAdviceTest {

    private final XssRequestBodyAdvice advice = new XssRequestBodyAdvice();

    @Test
    void shouldSupportEveryRequestBody() {
        assertEquals(true, advice.supports(null, null, (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class));
    }

    @Test
    void shouldRejectMaliciousDigitalServiceVersionPayload() throws IOException {
        String payload = """
                {
                  "dsName": "<script>alert('test')</script>",
                  "versionName": "string",
                  "isAi": true,
                  "note": {
                    "content": "string",
                    "creation_date": "2026-11-11T14:56:33.763Z",
                    "last_update_date": "2026-11-11T14:56:33.763Z"
                  }
                }
                """;

        HttpInputMessage inputMessage = new MockHttpInputMessage(payload.getBytes(StandardCharsets.UTF_8));

        assertThrows(G4itRestException.class,
                () -> advice.beforeBodyRead(inputMessage, mockParameter(), Object.class, null));
    }

    @Test
    void shouldRejectNestedScriptDeepInBody() throws IOException {
        String payload = """
                {
                  "note": {
                    "content": "<p><script>alert(1)</script></p>"
                  }
                }
                """;

        HttpInputMessage inputMessage = new MockHttpInputMessage(payload.getBytes(StandardCharsets.UTF_8));

        assertThrows(G4itRestException.class,
                () -> advice.beforeBodyRead(inputMessage, mockParameter(), Object.class, null));
    }

    @Test
    void shouldRejectScriptInsideArray() throws IOException {
        String payload = """
                {
                  "tags": ["safe", "<script>alert(1)</script>"]
                }
                """;

        HttpInputMessage inputMessage = new MockHttpInputMessage(payload.getBytes(StandardCharsets.UTF_8));

        assertThrows(G4itRestException.class,
                () -> advice.beforeBodyRead(inputMessage, mockParameter(), Object.class, null));
    }

    @Test
    void shouldAllowSafePayloadAndReplayTheBody() throws IOException {
        String payload = """
                {
                  "dsName": "My service",
                  "versionName": "v1",
                  "isAi": false
                }
                """;

        HttpInputMessage inputMessage = new MockHttpInputMessage(payload.getBytes(StandardCharsets.UTF_8));

        HttpInputMessage result = advice.beforeBodyRead(inputMessage, mockParameter(), Object.class, null);

        // body must still be readable downstream by the real Jackson converter
        String replayed = new String(result.getBody().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(payload, replayed);
    }

    private MethodParameter mockParameter() {
        return null;
    }
}

