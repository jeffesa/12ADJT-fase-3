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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUpcomingAppointmentHistoryByPatientIdUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindUpcomingAppointmentHistoryByPatientIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindUpcomingAppointmentHistoryByPatientIdUseCase(appointmentHistoryGateway);
    }

    @Test
    @DisplayName("Paciente vê as próprias consultas futuras")
    void patientSeesOwnUpcoming() {
        UUID patientId = UUID.randomUUID();
        List<AppointmentHistory> expected = List.of(new AppointmentHistory());
        when(appointmentHistoryGateway.findUpcomingByPatientId(patientId)).thenReturn(expected);

        List<AppointmentHistory> result = useCase.execute(patientId, patientId, UserRole.ROLE_PATIENT);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findUpcomingByPatientId(patientId);
    }

    @Test
    @DisplayName("Médico pode ver consultas futuras de qualquer paciente")
    void doctorSeesAnyUpcoming() {
        UUID patientId = UUID.randomUUID();
        when(appointmentHistoryGateway.findUpcomingByPatientId(patientId)).thenReturn(List.of());

        useCase.execute(patientId, UUID.randomUUID(), UserRole.ROLE_DOCTOR);

        verify(appointmentHistoryGateway).findUpcomingByPatientId(patientId);
    }

    @Test
    @DisplayName("Paciente NÃO pode ver consultas futuras de outro paciente")
    void patientCannotSeeOtherUpcoming() {
        UUID patientId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(patientId, UUID.randomUUID(), UserRole.ROLE_PATIENT));

        verify(appointmentHistoryGateway, never()).findUpcomingByPatientId(patientId);
    }

    @Test
    @DisplayName("Role nula é rejeitada")
    void nullRoleRejected() {
        UUID patientId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(patientId, patientId, null));

        verify(appointmentHistoryGateway, never()).findUpcomingByPatientId(patientId);
    }
}
