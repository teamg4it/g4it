/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apiuser.business.AuthService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AuthenticationFilter}, in particular the fix that stops
 * leaking raw exception messages to the client via {@code sendError}
 * (Checkmarx Information Exposure Through Sent Data finding).
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    @Test
    void doFilterInternal_happyPath_continuesFilterChain() throws Exception {
        UserBO userInfo = UserBO.builder().id(1L).email("user@test.com").build();

        when(request.getRequestURI()).thenReturn("/api/organizations/ORG/workspaces/1/inventories");
        when(authService.verifyUserAuthentication()).thenReturn(userInfo);
        when(authService.getOrganizationAndWorkspace(any())).thenReturn(Pair.of("ORG", "1"));
        when(authService.getJwtToken(eq(userInfo), eq("ORG"), eq(1L))).thenReturn(mock(JwtAuthenticationToken.class));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void doFilterInternal_authorizationException_sendsGenericMessage_notRawExceptionDetail() throws Exception {
        String sensitiveDetail = "Internal DB connection string: jdbc://secret-host/db?pwd=12345";
        when(authService.verifyUserAuthentication())
                .thenThrow(new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, sensitiveDetail));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        // Client must receive a generic message, never the raw (potentially sensitive) exception detail
        verify(response, times(1)).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), eq("Access denied."));
        verify(response, never()).sendError(anyInt(), contains("jdbc"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_unexpectedException_sendsGenericMessage_notRawExceptionDetail() throws Exception {
        String sensitiveDetail = "NullPointerException at com.soprasteria.internal.SecretClass.line42";
        when(authService.verifyUserAuthentication()).thenThrow(new RuntimeException(sensitiveDetail));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).sendError(eq(500), eq("Internal server error."));
        verify(response, never()).sendError(anyInt(), contains("SecretClass"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldNotFilter_whitelistedPath_returnsTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/version");

        assertTrue(authenticationFilter.shouldNotFilter(request));
    }
}
