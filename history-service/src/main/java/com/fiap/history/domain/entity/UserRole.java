package com.fiap.history.domain.entity;

/**
 * Papéis de usuário reconhecidos pelo history-service.
 * Espelha as roles emitidas pelo scheduling-service no JWT.
 */
public enum UserRole {
    ROLE_PATIENT,
    ROLE_DOCTOR,
    ROLE_NURSE;

    /**
     * Converte a string de role vinda do JWT (ex.: "PATIENT" ou "ROLE_PATIENT") no enum.
     * Retorna null se a role for desconhecida/nula.
     */
    public static UserRole fromToken(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
