package com.fiap.scheduling.domain.shared;

/**
 * Interface de domínio para hashing de senhas.
 * Implementação na camada de infraestrutura (BCrypt).
 */
public interface PasswordHasher {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
