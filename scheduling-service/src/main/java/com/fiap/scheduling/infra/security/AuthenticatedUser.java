package com.fiap.scheduling.infra.security;

import java.util.UUID;

/**
 * Objeto que representa o usuário autenticado no SecurityContext.
 * Usado como principal na autenticação JWT.
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String role
) {
}
