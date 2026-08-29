package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;

import java.util.List;

/**
 * Caso de uso: listar usuários.
 * Se role for informada, filtra por role; senão, lista todos.
 */
public class FindAllUsersUseCase {

    private final UserGateway userGateway;

    public FindAllUsersUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public List<User> execute(UserRole roleFilter) {
        if (roleFilter != null) {
            return userGateway.findByRole(roleFilter);
        }
        return userGateway.findAll();
    }
}
