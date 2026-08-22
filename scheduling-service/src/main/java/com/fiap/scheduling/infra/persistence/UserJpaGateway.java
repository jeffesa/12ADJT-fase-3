package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do UserGateway usando JPA.
 */
@Component
public class UserJpaGateway implements UserGateway {

    private final UserRepository userRepository;

    public UserJpaGateway(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        UserJpaEntity saved = userRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserJpaEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }
}
