package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.entity.UserRole;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import com.fiap.history.domain.shared.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllAppointmentHistoryUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindAllAppointmentHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllAppointmentHistoryUseCase(appointmentHistoryGateway);
    }

    @Test
    @DisplayName("Médico lista todo o histórico")
    void doctorListsAll() {
        List<AppointmentHistory> expected = List.of(new AppointmentHistory(), new AppointmentHistory());
        when(appointmentHistoryGateway.findAll()).thenReturn(expected);

        List<AppointmentHistory> result = useCase.execute(UserRole.ROLE_DOCTOR);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findAll();
    }

    @Test
    @DisplayName("Enfermeiro lista todo o histórico")
    void nurseListsAll() {
        when(appointmentHistoryGateway.findAll()).thenReturn(List.of());

        useCase.execute(UserRole.ROLE_NURSE);

        verify(appointmentHistoryGateway).findAll();
    }

    @Test
    @DisplayName("Paciente NÃO pode listar todo o histórico")
    void patientCannotListAll() {
        assertThrows(BusinessException.class, () -> useCase.execute(UserRole.ROLE_PATIENT));
        verify(appointmentHistoryGateway, never()).findAll();
    }

    @Test
    @DisplayName("Role nula é rejeitada")
    void nullRoleRejected() {
        assertThrows(BusinessException.class, () -> useCase.execute(null));
        verify(appointmentHistoryGateway, never()).findAll();
    }
}
