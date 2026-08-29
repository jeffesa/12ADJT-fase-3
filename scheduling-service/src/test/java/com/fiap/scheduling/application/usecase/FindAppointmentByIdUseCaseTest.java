package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAppointmentByIdUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private FindAppointmentByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentByIdUseCase(appointmentGateway);
    }

    @Test
    void shouldAllowPatientToSeeOwnAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));

        Appointment result = useCase.execute(appointmentId, patientId, UserRole.ROLE_PATIENT);

        assertEquals(appointmentId, result.getId());
    }

    @Test
    void shouldRejectPatientViewingAnotherAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID anotherPatientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(anotherPatientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(appointmentId, patientId, UserRole.ROLE_PATIENT));

        assertEquals("Paciente só pode visualizar suas próprias consultas", exception.getMessage());
    }

    @Test
    void shouldAllowDoctorToSeeAnyAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));

        Appointment result = useCase.execute(appointmentId, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(appointmentId, result.getId());
    }

    @Test
    void shouldThrowWhenAppointmentDoesNotExist() {
        when(appointmentGateway.findById(any(UUID.class))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(UUID.randomUUID(), UUID.randomUUID(), UserRole.ROLE_NURSE));

        assertEquals("Consulta não encontrada", exception.getMessage());
    }
}
