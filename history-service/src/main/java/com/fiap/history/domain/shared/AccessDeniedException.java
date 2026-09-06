package com.fiap.history.domain.shared;

/**
 * Lançada quando um usuário autenticado não tem permissão (role/ownership)
 * para executar a operação. Mapeada para HTTP 403 (Forbidden).
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
