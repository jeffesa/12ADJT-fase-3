package com.fiap.scheduling.domain.shared;

import java.util.UUID;

/**
 * Interface de domínio para geração e validação de tokens JWT.
 * Implementação na camada de infraestrutura.
 */
public interface TokenProvider {

    String generateToken(UUID userId, String email, String role);

    boolean validateToken(String token);

    String getEmailFromToken(String token);

    String getRoleFromToken(String token);

    UUID getUserIdFromToken(String token);
}
