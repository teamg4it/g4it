/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SharedLinkValidationFilter extends OncePerRequestFilter {

    @Autowired
    DigitalServiceLinkRepository digitalServiceLinkRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/shared/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Example URI: /share/{shareId}/ds/{digitalServiceId}
            String[] urlSplit = request.getRequestURI().split("/");
            if (urlSplit.length < 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid shared link format\"}");
                response.flushBuffer();
                return;
            }

            String shareId = urlSplit[2];   // /share/{shareId}
            String digitalServiceId = urlSplit[4]; // /ds/{digitalServiceId}

            boolean valid = digitalServiceLinkRepository.validateLink(shareId, digitalServiceId).isPresent();

            if (!valid) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Shared link expired or invalid\"}");
                response.flushBuffer();
                return;
            }

        } catch (IllegalArgumentException e) { // invalid UUID
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid UUID format in shared link\"}");
            response.flushBuffer();
            return;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unexpected error\"}");
            response.flushBuffer();
            return;
        }

        filterChain.doFilter(request, response);
    }

}
