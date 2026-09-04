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
class FindAppointmentHistoryByDoctorIdUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindAppointmentHistoryByDoctorIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentHistoryByDoctorIdUseCase(appointmentHistoryGateway);
    }

    @Test
    @DisplayName("Médico vê o próprio histórico")
    void doctorSeesOwnHistory() {
        UUID doctorId = UUID.randomUUID();
        List<AppointmentHistory> expected = List.of(new AppointmentHistory());
        when(appointmentHistoryGateway.findByDoctorId(doctorId)).thenReturn(expected);

        List<AppointmentHistory> result = useCase.execute(doctorId, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findByDoctorId(doctorId);
    }

    @Test
    @DisplayName("Enfermeiro pode ver histórico de qualquer médico")
    void nurseSeesAnyDoctorHistory() {
        UUID doctorId = UUID.randomUUID();
        when(appointmentHistoryGateway.findByDoctorId(doctorId)).thenReturn(List.of());

        useCase.execute(doctorId, UUID.randomUUID(), UserRole.ROLE_NURSE);

        verify(appointmentHistoryGateway).findByDoctorId(doctorId);
    }

    @Test
    @DisplayName("Médico NÃO pode ver histórico de outro médico")
    void doctorCannotSeeOtherDoctor() {
        UUID doctorId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(doctorId, UUID.randomUUID(), UserRole.ROLE_DOCTOR));

        verify(appointmentHistoryGateway, never()).findByDoctorId(doctorId);
    }

    @Test
    @DisplayName("Paciente NÃO pode consultar histórico por médico")
    void patientCannotQueryByDoctor() {
        UUID doctorId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(doctorId, UUID.randomUUID(), UserRole.ROLE_PATIENT));

        verify(appointmentHistoryGateway, never()).findByDoctorId(doctorId);
    }

    @Test
    @DisplayName("Role nula é rejeitada")
    void nullRoleRejected() {
        UUID doctorId = UUID.randomUUID();

        assertThrows(BusinessException.class,
                () -> useCase.execute(doctorId, doctorId, null));

        verify(appointmentHistoryGateway, never()).findByDoctorId(doctorId);
    }
}
