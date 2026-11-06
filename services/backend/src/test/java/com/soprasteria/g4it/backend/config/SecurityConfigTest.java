/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SecurityConfig}.
 * <p>
 * Building the full {@code filterChain()} bean requires a live Spring Security
 * {@code HttpSecurity} instance wired by an application context (OAuth2 issuer,
 * JWT decoder, etc.), so it is exercised by the Spring Boot context / functional
 * tests at application startup. This test focuses on the Checkmarx fix itself:
 * the Content-Security-Policy directive value must stay present and restrictive
 * (Checkmarx "Content Security Policy is not explicitly defined" finding).
 * <p>
 * To confirm the header is actually sent in a running environment, verify with:
 * {@code curl -I https://<host>/version} and check for a
 * {@code Content-Security-Policy} response header.
 */
class SecurityConfigTest {

    private static final String EXPECTED_CSP = "default-src 'self'; frame-ancestors 'none'";

    @Test
    void contentSecurityPolicy_isDefinedAndRestrictive() {
        String csp = (String) ReflectionTestUtils.getField(SecurityConfig.class, "CONTENT_SECURITY_POLICY");

        assertEquals(EXPECTED_CSP, csp);
        assertTrue(csp.contains("default-src 'self'"), "CSP must restrict default content sources to the app's own origin");
        assertTrue(csp.contains("frame-ancestors 'none'"), "CSP must prevent the app from being embedded in frames (clickjacking protection)");
    }

    @Test
    void corsConfigurationSource_restrictsToConfiguredOrigins() {
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "corsAllowedOrigins", List.of("http://localhost:4200"));

        UrlBasedCorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfigurations().get("/**");

        assertEquals(List.of("http://localhost:4200"), configuration.getAllowedOrigins());
        assertEquals(List.of("*"), configuration.getAllowedMethods());
        assertEquals(List.of("*"), configuration.getAllowedHeaders());
    }
}
