package com.fiap.scheduling.infra.web.dto;

import java.util.UUID;

/**
 * DTO para response de autenticação (register e login).
 * Contém o JWT token e dados do usuário.
 */
public record AuthResponse(
        String token,
        UUID userId,
        String name,
        String email,
        String role
) {
}
