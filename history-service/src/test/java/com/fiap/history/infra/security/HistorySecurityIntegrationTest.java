package com.fiap.history.infra.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

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

    // Mesma secret default do application.yml (perfil test) — token gerado com ela
    // é reconhecido pelo validador, mas expira imediatamente.
    private static final String SECRET = "dGVjaC1jaGFsbGVuZ2UtZmFzZTMtand0LXNlY3JldC1rZXktMjAyNi1maWFwLXByb2plY3Q=";

    private String expiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        Date past = new Date(System.currentTimeMillis() - 60_000); // 1 min atrás
        return Jwts.builder()
                .subject("expired@fiap.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_DOCTOR")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(past)
                .signWith(key)
                .compact();
    }

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

    @Test
    @DisplayName("POST /graphql com token expirado → 401")
    void graphqlWithExpiredTokenReturns401() throws Exception {
        mockMvc.perform(post("/graphql")
                        .header("Authorization", "Bearer " + expiredToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUERY))
                .andExpect(status().isUnauthorized());
    }
}
