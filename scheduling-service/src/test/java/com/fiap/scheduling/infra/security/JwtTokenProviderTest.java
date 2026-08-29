package com.fiap.scheduling.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    // Secret Base64 de teste (>= 256 bits para HS384/HS512)
    private static final String SECRET =
            "dGVjaC1jaGFsbGVuZ2UtZmFzZTMtand0LXNlY3JldC1rZXktMjAyNi1maWFwLXByb2plY3Q=";

    private JwtTokenProvider providerWithExpiration(long expirationMs) {
        return new JwtTokenProvider(SECRET, expirationMs);
    }

    @Test
    @DisplayName("Deve gerar token válido e extrair os claims")
    void shouldGenerateAndReadClaims() {
        JwtTokenProvider provider = providerWithExpiration(86_400_000);
        UUID userId = UUID.randomUUID();

        String token = provider.generateToken(userId, "joao@mail.com", "ROLE_DOCTOR");

        assertTrue(provider.validateToken(token));
        assertEquals("joao@mail.com", provider.getEmailFromToken(token));
        assertEquals("ROLE_DOCTOR", provider.getRoleFromToken(token));
        assertEquals(userId, provider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Deve invalidar token malformado")
    void shouldRejectMalformedToken() {
        JwtTokenProvider provider = providerWithExpiration(86_400_000);

        assertFalse(provider.validateToken("isto.nao.e.um.jwt"));
        assertFalse(provider.validateToken("qualquercoisa"));
    }

    @Test
    @DisplayName("Deve invalidar token assinado com outra secret")
    void shouldRejectTokenWithWrongSignature() {
        JwtTokenProvider issuer = providerWithExpiration(86_400_000);
        String token = issuer.generateToken(UUID.randomUUID(), "a@b.com", "ROLE_NURSE");

        // Outro provider com secret diferente
        String otherSecret = "b3V0cmEtc2VjcmV0LWtleS1kaWZlcmVudGUtcGFyYS10ZXN0ZS0xMjM0NTY3ODkw";
        JwtTokenProvider validator = new JwtTokenProvider(otherSecret, 86_400_000);

        assertFalse(validator.validateToken(token));
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void shouldRejectExpiredToken() throws InterruptedException {
        // expiração de 1ms — expira praticamente na hora
        JwtTokenProvider provider = providerWithExpiration(1);
        String token = provider.generateToken(UUID.randomUUID(), "a@b.com", "ROLE_PATIENT");

        Thread.sleep(50);

        assertFalse(provider.validateToken(token));
    }
}
