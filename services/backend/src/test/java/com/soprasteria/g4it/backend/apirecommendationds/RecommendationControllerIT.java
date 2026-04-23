package com.soprasteria.g4it.backend.apirecommendationds;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.soprasteria.g4it.backend.apirecommendationds.business.RecommendationService;
import com.soprasteria.g4it.backend.server.gen.api.dto.RecommendationDSRest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;



@Test
void shouldAllowAccess_forNonSuperAdmin() throws Exception {

    // 1. Simuler un utilisateur NON superadmin
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    var authentication = new UsernamePasswordAuthenticationToken(
            "normal.user@test.com",   // principal
            null,
            authorities
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    // 2. Mock service métier
    when(recommendationService.getRecommendations("org-test", 1L))
            .thenReturn(List.of(new RecommendationDSRest()));

    // 3. Appel API
    mockMvc.perform(
            get("/api/v1/organizations/org-test/workspaces/1/recommendations")
    )
    .andExpect(status().isOk());
}

    @Test
    void shouldAllowAccess_forNormalUser() throws Exception {

        // Mock service return
        when(recommendationService.getRecommendations("org-test", 1L))
                .thenReturn(List.of(new RecommendationDSRest()));

        mockMvc.perform(get("/api/v1/organizations/org-test/workspaces/1/recommendations")
                        .header("Authorization", "Bearer fake-jwt-user"))
                .andExpect(status().isOk());
    }

    
}
