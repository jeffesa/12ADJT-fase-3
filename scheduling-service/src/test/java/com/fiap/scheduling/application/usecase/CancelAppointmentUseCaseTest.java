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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private CancelAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelAppointmentUseCase(appointmentGateway);
    }

    @Test
    void shouldAllowDoctorToCancelAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentGateway.update(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(appointmentId, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(com.fiap.scheduling.domain.entity.AppointmentStatus.CANCELLED, result.getStatus());
    }

    @Test
    void shouldAllowPatientToCancelOwnAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentGateway.update(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(appointmentId, patientId, UserRole.ROLE_PATIENT);

        assertEquals(com.fiap.scheduling.domain.entity.AppointmentStatus.CANCELLED, result.getStatus());
    }

    @Test
    void shouldRejectWhenPatientTriesToCancelAnotherAppointment() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.create(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now().plusDays(1), "Consulta");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(appointmentId, UUID.randomUUID(), UserRole.ROLE_PATIENT));

        assertEquals("Usuário sem permissão para cancelar esta consulta", exception.getMessage());
    }

    @Test
    void shouldRejectWhenAppointmentDoesNotExist() {
        when(appointmentGateway.findById(any(UUID.class))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(UUID.randomUUID(), UUID.randomUUID(), UserRole.ROLE_NURSE));

        assertEquals("Consulta não encontrada", exception.getMessage());
    }
}
