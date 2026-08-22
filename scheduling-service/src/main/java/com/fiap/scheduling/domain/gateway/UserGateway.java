package com.fiap.scheduling.domain.gateway;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de gateway para persistência de usuários.
 * Implementação na camada de infraestrutura.
 */
public interface UserGateway {

    User create(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(UserRole role);
}
