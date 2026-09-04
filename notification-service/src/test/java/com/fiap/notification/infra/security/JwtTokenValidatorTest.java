package com.fiap.notification.infra.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenValidatorTest {

    private static final String SECRET =
            "dGVjaC1jaGFsbGVuZ2UtZmFzZTMtand0LXNlY3JldC1rZXktMjAyNi1maWFwLXByb2plY3Q=";

    private JwtTokenValidator validator;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator(SECRET);
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    private String token(UUID userId, String email, String role, long expiresInMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiresInMs))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Token válido → validateToken true e claims corretas")
    void validTokenParsesClaims() {
        UUID userId = UUID.randomUUID();
        String t = token(userId, "user@mail.com", "ROLE_DOCTOR", 60_000);

        assertThat(validator.validateToken(t)).isTrue();
        assertThat(validator.getEmailFromToken(t)).isEqualTo("user@mail.com");
        assertThat(validator.getRoleFromToken(t)).isEqualTo("ROLE_DOCTOR");
        assertThat(validator.getUserIdFromToken(t)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Token expirado → validateToken false")
    void expiredTokenIsInvalid() {
        String t = token(UUID.randomUUID(), "user@mail.com", "ROLE_PATIENT", -1_000);
        assertThat(validator.validateToken(t)).isFalse();
    }

    @Test
    @DisplayName("Token malformado → validateToken false")
    void malformedTokenIsInvalid() {
        assertThat(validator.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    @DisplayName("Token assinado com outra chave → validateToken false")
    void wrongSignatureIsInvalid() {
        SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                "b3V0cmEtY2hhdmUtc2VjcmV0YS1wYXJhLXRlc3RlLWRlLWFzc2luYXR1cmEtaW52YWxpZGE="));
        String t = Jwts.builder()
                .subject("x@mail.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_PATIENT")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        assertThat(validator.validateToken(t)).isFalse();
    }
}
