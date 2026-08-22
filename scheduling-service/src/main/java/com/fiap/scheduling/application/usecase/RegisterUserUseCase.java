package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.PasswordHasher;

/**
 * Caso de uso: Registro de novo usuário.
 * Valida email único, hash da senha e role obrigatória.
 */
public class RegisterUserUseCase {

    private final UserGateway userGateway;
    private final PasswordHasher passwordHasher;

    public RegisterUserUseCase(UserGateway userGateway, PasswordHasher passwordHasher) {
        this.userGateway = userGateway;
        this.passwordHasher = passwordHasher;
    }

    public User execute(String name, String email, String rawPassword, UserRole role) {
        // Validar que email é único
        userGateway.findByEmail(email).ifPresent(existing -> {
            throw new BusinessException("Email já cadastrado: " + email);
        });

        // Criar usuário com senha hasheada
        String encodedPassword = passwordHasher.encode(rawPassword);
        User user = User.create(name, email, encodedPassword, role);

        // Validações de domínio
        user.validateName();
        user.validateEmail();

        return userGateway.create(user);
    }
}
