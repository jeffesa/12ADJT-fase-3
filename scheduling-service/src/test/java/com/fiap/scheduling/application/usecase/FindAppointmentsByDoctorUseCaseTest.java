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
class FindAppointmentsByDoctorUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private FindAppointmentsByDoctorUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentsByDoctorUseCase(appointmentGateway);
    }

    @Test
    void shouldAllowDoctorToSeeOwnAppointments() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        when(appointmentGateway.findByDoctorId(doctorId)).thenReturn(List.of(appointment));

        List<Appointment> result = useCase.execute(doctorId, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(1, result.size());
        assertEquals(doctorId, result.get(0).getDoctorId());
    }

    @Test
    void shouldAllowNurseToSeeAnyDoctorAppointments() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        when(appointmentGateway.findByDoctorId(doctorId)).thenReturn(List.of(appointment));

        List<Appointment> result = useCase.execute(doctorId, nurseId, UserRole.ROLE_NURSE);

        assertEquals(1, result.size());
    }

    @Test
    void shouldRejectDoctorViewingAnotherDoctorAppointments() {
        UUID doctorId = UUID.randomUUID();
        UUID anotherDoctorId = UUID.randomUUID();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(anotherDoctorId, doctorId, UserRole.ROLE_DOCTOR));

        assertEquals("Médico só pode visualizar suas próprias consultas", exception.getMessage());
    }
}
