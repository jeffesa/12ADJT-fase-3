package com.fiap.history.infra.security;

import java.util.UUID;

/**
 * Objeto que representa o usuário autenticado no SecurityContext.
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String role
) {
}
