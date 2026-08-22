package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.PasswordHasher;
import com.fiap.scheduling.domain.shared.TokenProvider;

/**
 * Caso de uso: Login do usuário.
 * Valida credenciais e retorna JWT token.
 */
public class LoginUseCase {

    private final UserGateway userGateway;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginUseCase(UserGateway userGateway, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.userGateway = userGateway;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public LoginResult execute(String email, String rawPassword) {
        User user = userGateway.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Email ou senha inválidos"));

        if (!passwordHasher.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("Email ou senha inválidos");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new LoginResult(token, user);
    }

    public record LoginResult(String token, User user) {
    }
}
