package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.PasswordHasher;
import com.fiap.scheduling.domain.shared.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserGateway userGateway;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenProvider tokenProvider;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(userGateway, passwordHasher, tokenProvider);
    }

    private User buildUser() {
        return new User(UUID.randomUUID(), "João", "joao@mail.com", "hash",
                UserRole.ROLE_DOCTOR, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve retornar token quando credenciais são válidas")
    void shouldLogin() {
        User user = buildUser();
        when(userGateway.findByEmail("joao@mail.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("senha123", "hash")).thenReturn(true);
        when(tokenProvider.generateToken(eq(user.getId()), eq("joao@mail.com"), eq("ROLE_DOCTOR")))
                .thenReturn("jwt-token");

        LoginUseCase.LoginResult result = useCase.execute("joao@mail.com", "senha123");

        assertEquals("jwt-token", result.token());
        assertEquals(user.getId(), result.user().getId());
    }

    @Test
    @DisplayName("Deve falhar quando email não existe")
    void shouldFailWhenEmailNotFound() {
        when(userGateway.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> useCase.execute("nao@existe.com", "senha123"));
        verify(tokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Deve falhar quando senha é inválida")
    void shouldFailWhenPasswordInvalid() {
        User user = buildUser();
        when(userGateway.findByEmail("joao@mail.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("errada", "hash")).thenReturn(false);

        assertThrows(BusinessException.class, () -> useCase.execute("joao@mail.com", "errada"));
        verify(tokenProvider, never()).generateToken(any(), any(), any());
    }
}
