package com.fiap.scheduling.infra.config;

import com.fiap.scheduling.application.usecase.LoginUseCase;
import com.fiap.scheduling.application.usecase.RegisterUserUseCase;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.PasswordHasher;
import com.fiap.scheduling.domain.shared.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de beans para use cases.
 * Use cases são POJOs puros — registrados aqui como Spring beans.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserGateway userGateway, PasswordHasher passwordHasher) {
        return new RegisterUserUseCase(userGateway, passwordHasher);
    }

    @Bean
    public LoginUseCase loginUseCase(UserGateway userGateway, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        return new LoginUseCase(userGateway, passwordHasher, tokenProvider);
    }
}
