package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private PasswordHasher passwordHasher;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userGateway, passwordHasher);
    }

    @Test
    @DisplayName("Deve registrar usuário com senha hasheada quando email é único")
    void shouldRegisterUser() {
        when(userGateway.findByEmail("novo@mail.com")).thenReturn(Optional.empty());
        when(passwordHasher.encode("senha123")).thenReturn("hash");
        when(userGateway.create(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = useCase.execute("João", "novo@mail.com", "senha123", UserRole.ROLE_DOCTOR);

        assertEquals("João", result.getName());
        assertEquals("hash", result.getPassword());
        assertEquals(UserRole.ROLE_DOCTOR, result.getRole());
        verify(passwordHasher).encode("senha123");
        verify(userGateway).create(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando email já cadastrado")
    void shouldFailWhenEmailExists() {
        when(userGateway.findByEmail("existe@mail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(BusinessException.class,
                () -> useCase.execute("João", "existe@mail.com", "senha123", UserRole.ROLE_PATIENT));

        verify(userGateway, never()).create(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando email é inválido")
    void shouldFailWhenEmailInvalid() {
        when(userGateway.findByEmail("invalido")).thenReturn(Optional.empty());
        when(passwordHasher.encode(anyString())).thenReturn("hash");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("João", "invalido", "senha123", UserRole.ROLE_PATIENT));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é vazio")
    void shouldFailWhenNameBlank() {
        when(userGateway.findByEmail("ok@mail.com")).thenReturn(Optional.empty());
        when(passwordHasher.encode(anyString())).thenReturn("hash");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("  ", "ok@mail.com", "senha123", UserRole.ROLE_PATIENT));
    }
}
