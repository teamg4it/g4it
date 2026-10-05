/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.common.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
/**
 * Security Configuration
 */
@Configuration
@Profile("!test")
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    Environment environment;

    @Value("${cors.allowed.origins}")
    private List<String> corsAllowedOrigins;

    /**
     * URI WhiteList.
     */
    private static final String[] AUTH_WHITELIST = {
            "/v3/api-docs",
            "/v3/api-docs/swagger-config",
            "/swagger-ui/**",
            "/api/v3/api-docs",
            "/api/v3/api-docs/swagger-config",
            "/api/swagger-ui/**",
            "/actuator/**",
            "/shared/**"
    };

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsAllowedOrigins);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        if (environment.matchesProfiles(Constants.NOSECURITY)) {
            return http
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(Customizer.withDefaults())
                    .build();
        } else {
            return http
                    .authorizeHttpRequests(auth -> auth.requestMatchers(AUTH_WHITELIST).permitAll())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                    .cors(Customizer.withDefaults())
                    .build();
        }

    }

}