package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAppointmentsByPatientUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private FindAppointmentsByPatientUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentsByPatientUseCase(appointmentGateway);
    }

    @Test
    void shouldAllowPatientToSeeOwnAppointments() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        when(appointmentGateway.findByPatientId(patientId)).thenReturn(List.of(appointment));

        List<Appointment> result = useCase.execute(patientId, patientId, UserRole.ROLE_PATIENT);

        assertEquals(1, result.size());
        assertEquals(patientId, result.get(0).getPatientId());
    }

    @Test
    void shouldAllowDoctorToSeeAnyPatientAppointments() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        when(appointmentGateway.findByPatientId(patientId)).thenReturn(List.of(appointment));

        List<Appointment> result = useCase.execute(patientId, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(1, result.size());
    }

    @Test
    void shouldRejectPatientRequestingAnotherPatientAppointments() {
        UUID patientId = UUID.randomUUID();
        UUID anotherPatientId = UUID.randomUUID();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(anotherPatientId, patientId, UserRole.ROLE_PATIENT));

        assertEquals("Paciente só pode visualizar suas próprias consultas", exception.getMessage());
    }
}
