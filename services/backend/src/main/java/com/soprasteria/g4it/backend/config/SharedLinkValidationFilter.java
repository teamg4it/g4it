/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceSharedLink;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

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
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid shared link format");
                return;
            }

            String shareId = urlSplit[2];   // /share/{shareId}
            String digitalServiceId = urlSplit[4]; // /ds/{digitalServiceId}

            //boolean valid = digitalServiceLinkRepository.validateLink(shareId, digitalServiceId).isPresent();
            Optional<DigitalServiceSharedLink> digitalServiceSharedLink = digitalServiceLinkRepository.findByUidAndDigitalService_Uid(shareId, digitalServiceId);

            if (digitalServiceSharedLink.isEmpty()) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Shared link does not exist");
                return;
            } else if (digitalServiceSharedLink.get().getExpiryDate().isBefore(LocalDateTime.now())) {
                writeError(response, HttpServletResponse.SC_GONE, "Shared link expired");
                return;
            }

        } catch (IllegalArgumentException e) { // invalid UUID
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format in shared link");
            return;
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        response.flushBuffer();
    }
}
