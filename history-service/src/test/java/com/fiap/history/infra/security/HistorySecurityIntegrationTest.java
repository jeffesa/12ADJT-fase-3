package com.fiap.history.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de segurança HTTP do history-service (TASK-030).
 *
 * Uma requisição ao /graphql sem token (ou com token inválido) deve ser barrada
 * pelo Spring Security com 401 (não autenticado), via RestAuthenticationEntryPoint.
 * Os cenários de ownership (403) são cobertos em HistoryGraphQlIntegrationTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistorySecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String QUERY = "{\"query\":\"query { allAppointmentHistories { id } }\"}";

    @Test
    @DisplayName("POST /graphql sem token → 401")
    void graphqlWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUERY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /graphql com token inválido → 401")
    void graphqlWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(post("/graphql")
                        .header("Authorization", "Bearer token-invalido-xyz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUERY))
                .andExpect(status().isUnauthorized());
    }
}
