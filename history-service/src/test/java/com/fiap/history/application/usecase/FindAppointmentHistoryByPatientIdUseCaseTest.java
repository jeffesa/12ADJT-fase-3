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
class FindAppointmentHistoryByPatientIdUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindAppointmentHistoryByPatientIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentHistoryByPatientIdUseCase(appointmentHistoryGateway);
    }

    @Test
    @DisplayName("Paciente vê o próprio histórico")
    void patientSeesOwnHistory() {
        UUID patientId = UUID.randomUUID();
        List<AppointmentHistory> expected = List.of(new AppointmentHistory());
        when(appointmentHistoryGateway.findByPatientId(patientId)).thenReturn(expected);

        List<AppointmentHistory> result = useCase.execute(patientId, patientId, UserRole.ROLE_PATIENT);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findByPatientId(patientId);
    }

    @Test
    @DisplayName("Médico pode ver histórico de qualquer paciente")
    void doctorSeesAnyPatientHistory() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        when(appointmentHistoryGateway.findByPatientId(patientId)).thenReturn(List.of());

        useCase.execute(patientId, doctorId, UserRole.ROLE_DOCTOR);

        verify(appointmentHistoryGateway).findByPatientId(patientId);
    }

    @Test
    @DisplayName("Enfermeiro pode ver histórico de qualquer paciente")
    void nurseSeesAnyPatientHistory() {
        UUID patientId = UUID.randomUUID();
        when(appointmentHistoryGateway.findByPatientId(patientId)).thenReturn(List.of());

        useCase.execute(patientId, UUID.randomUUID(), UserRole.ROLE_NURSE);

        verify(appointmentHistoryGateway).findByPatientId(patientId);
    }

    @Test
    @DisplayName("Paciente NÃO pode ver histórico de outro paciente")
    void patientCannotSeeOtherPatient() {
        UUID patientId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(patientId, otherUserId, UserRole.ROLE_PATIENT));

        verify(appointmentHistoryGateway, never()).findByPatientId(patientId);
    }

    @Test
    @DisplayName("Role nula é rejeitada")
    void nullRoleRejected() {
        UUID patientId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(patientId, patientId, null));

        verify(appointmentHistoryGateway, never()).findByPatientId(patientId);
    }
}
