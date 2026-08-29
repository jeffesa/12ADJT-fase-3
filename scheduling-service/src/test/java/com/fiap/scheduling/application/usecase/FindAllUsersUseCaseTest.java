package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllUsersUseCaseTest {

    @Mock
    private UserGateway userGateway;

    private FindAllUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllUsersUseCase(userGateway);
    }

    @Test
    @DisplayName("Deve listar todos quando role não informada")
    void shouldListAll() {
        when(userGateway.findAll()).thenReturn(List.of(new User(), new User()));

        List<User> result = useCase.execute(null);

        assertEquals(2, result.size());
        verify(userGateway).findAll();
        verify(userGateway, never()).findByRole(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve filtrar por role quando informada")
    void shouldFilterByRole() {
        when(userGateway.findByRole(UserRole.ROLE_PATIENT)).thenReturn(List.of(new User()));

        List<User> result = useCase.execute(UserRole.ROLE_PATIENT);

        assertEquals(1, result.size());
        verify(userGateway).findByRole(UserRole.ROLE_PATIENT);
        verify(userGateway, never()).findAll();
    }
}
