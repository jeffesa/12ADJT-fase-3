package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.event.AppointmentEvent;
import com.fiap.scheduling.domain.event.EventPublisher;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.AccessDeniedException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;
    @Mock
    private EventPublisher eventPublisher;

    private ConfirmAppointmentUseCase useCase;

    private final UUID patientId = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ConfirmAppointmentUseCase(appointmentGateway, eventPublisher);
    }

    @Test
    void shouldConfirmScheduledAppointmentAsDoctorAndPublishEvent() {
        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        UUID id = appointment.getId();
        when(appointmentGateway.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentGateway.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(id, doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        verify(eventPublisher).publish(any(AppointmentEvent.class));
        verify(appointmentGateway).update(appointment);
    }

    @Test
    void shouldAllowNurseToConfirm() {
        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        UUID id = appointment.getId();
        when(appointmentGateway.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentGateway.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(id, UUID.randomUUID(), UserRole.ROLE_NURSE);

        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void shouldRejectPatient() {
        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(UUID.randomUUID(), patientId, UserRole.ROLE_PATIENT));
        verify(appointmentGateway, never()).update(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldRejectDoctorConfirmingAnotherDoctorsAppointment() {
        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        UUID id = appointment.getId();
        when(appointmentGateway.findById(id)).thenReturn(Optional.of(appointment));

        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(id, UUID.randomUUID(), UserRole.ROLE_DOCTOR));
        verify(appointmentGateway, never()).update(any());
    }

    @Test
    void shouldThrowWhenAppointmentNotFound() {
        UUID id = UUID.randomUUID();
        when(appointmentGateway.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(id, doctorId, UserRole.ROLE_DOCTOR));
    }
}
