package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserByIdUseCaseTest {

    @Mock
    private UserGateway userGateway;

    private FindUserByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindUserByIdUseCase(userGateway);
    }

    private User user(UUID id, UserRole role) {
        return new User(id, "Nome", "e@mail.com", "hash", role, LocalDateTime.now());
    }

    @Test
    @DisplayName("DOCTOR pode consultar qualquer usuário")
    void doctorCanQueryAnyone() {
        UUID target = UUID.randomUUID();
        when(userGateway.findById(target)).thenReturn(Optional.of(user(target, UserRole.ROLE_PATIENT)));

        User result = useCase.execute(target, UUID.randomUUID(), UserRole.ROLE_DOCTOR);

        assertEquals(target, result.getId());
    }

    @Test
    @DisplayName("PATIENT pode consultar o próprio perfil")
    void patientCanQuerySelf() {
        UUID selfId = UUID.randomUUID();
        when(userGateway.findById(selfId)).thenReturn(Optional.of(user(selfId, UserRole.ROLE_PATIENT)));

        User result = useCase.execute(selfId, selfId, UserRole.ROLE_PATIENT);

        assertEquals(selfId, result.getId());
    }

    @Test
    @DisplayName("PATIENT NÃO pode consultar perfil de outro")
    void patientCannotQueryOthers() {
        UUID target = UUID.randomUUID();
        UUID self = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(target, self, UserRole.ROLE_PATIENT));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando usuário não existe")
    void shouldFailWhenNotFound() {
        UUID target = UUID.randomUUID();
        when(userGateway.findById(target)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(target, UUID.randomUUID(), UserRole.ROLE_NURSE));
    }
}
