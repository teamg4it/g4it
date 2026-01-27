/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 *//*


package com.soprasteria.g4it.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.exception.ErrorResponse;
import com.soprasteria.g4it.backend.exception.XssViolationException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component@Configuration
public class JacksonXssConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
        return builder -> builder.deserializerByType(
                String.class,
                new XssStringDeserializer()
        );
    }
@Order(1)
public class XssFilter implements Filter {

    private final ObjectMapper objectMapper;

    public XssFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(new XssRequestWrapper(httpRequest), response);
        }catch (XssViolationException ex) {

            ErrorResponse error = new ErrorResponse(
                    "INVALID_INPUT",
                    ex.getMessage(),
                    httpRequest.getRequestURI()
            );

            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(httpResponse.getWriter(), error);
        }
    }
}

*/
