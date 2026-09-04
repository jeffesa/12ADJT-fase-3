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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAppointmentHistoryByIdUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindAppointmentHistoryByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentHistoryByIdUseCase(appointmentHistoryGateway);
    }

    private AppointmentHistory historyOf(UUID patientId, UUID doctorId) {
        AppointmentHistory h = new AppointmentHistory();
        h.setId(UUID.randomUUID());
        h.setPatientId(patientId);
        h.setDoctorId(doctorId);
        return h;
    }

    @Test
    @DisplayName("Paciente vê o registro do qual é o paciente")
    void patientSeesOwnRecord() {
        UUID id = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        AppointmentHistory expected = historyOf(patientId, UUID.randomUUID());
        when(appointmentHistoryGateway.findById(id)).thenReturn(expected);

        AppointmentHistory result = useCase.execute(id, patientId, UserRole.ROLE_PATIENT);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findById(id);
    }

    @Test
    @DisplayName("Médico vê o registro do qual é o médico")
    void doctorSeesOwnRecord() {
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        AppointmentHistory expected = historyOf(UUID.randomUUID(), doctorId);
        when(appointmentHistoryGateway.findById(id)).thenReturn(expected);

        AppointmentHistory result = useCase.execute(id, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Enfermeiro vê qualquer registro")
    void nurseSeesAnyRecord() {
        UUID id = UUID.randomUUID();
        AppointmentHistory expected = historyOf(UUID.randomUUID(), UUID.randomUUID());
        when(appointmentHistoryGateway.findById(id)).thenReturn(expected);

        AppointmentHistory result = useCase.execute(id, UUID.randomUUID(), UserRole.ROLE_NURSE);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Paciente NÃO vê registro de outro paciente")
    void patientCannotSeeOtherRecord() {
        UUID id = UUID.randomUUID();
        AppointmentHistory record = historyOf(UUID.randomUUID(), UUID.randomUUID());
        when(appointmentHistoryGateway.findById(id)).thenReturn(record);

        assertThrows(BusinessException.class,
                () -> useCase.execute(id, UUID.randomUUID(), UserRole.ROLE_PATIENT));
    }

    @Test
    @DisplayName("Médico NÃO vê registro de outro médico")
    void doctorCannotSeeOtherRecord() {
        UUID id = UUID.randomUUID();
        AppointmentHistory record = historyOf(UUID.randomUUID(), UUID.randomUUID());
        when(appointmentHistoryGateway.findById(id)).thenReturn(record);

        assertThrows(BusinessException.class,
                () -> useCase.execute(id, UUID.randomUUID(), UserRole.ROLE_DOCTOR));
    }

    @Test
    @DisplayName("Role nula é rejeitada")
    void nullRoleRejected() {
        UUID id = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(id, UUID.randomUUID(), null));
    }
}
