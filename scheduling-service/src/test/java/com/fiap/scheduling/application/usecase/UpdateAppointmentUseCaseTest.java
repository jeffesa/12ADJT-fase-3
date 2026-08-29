package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.event.AppointmentEvent;
import com.fiap.scheduling.domain.event.EventPublisher;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.gateway.UserGateway;
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
class UpdateAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private EventPublisher eventPublisher;

    private UpdateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAppointmentUseCase(appointmentGateway, userGateway, eventPublisher);
    }

    @Test
    void shouldUpdateAppointmentWhenDoctorOwnsIt() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta inicial");
        appointment.setId(appointmentId);

        User patient = new User(patientId, "Paciente", "paciente@email.com", "hash", UserRole.ROLE_PATIENT, LocalDateTime.now());
        User doctor = new User(doctorId, "Doutor", "medico@email.com", "hash", UserRole.ROLE_DOCTOR, LocalDateTime.now());

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(userGateway.findById(patientId)).thenReturn(Optional.of(patient));
        when(userGateway.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentGateway.update(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(appointmentId, patientId, doctorId, futureDate, "Consulta atualizada", doctorId, UserRole.ROLE_DOCTOR);

        assertEquals("Consulta atualizada", result.getDescription());
        assertEquals(futureDate, result.getDateTime());
        verify(eventPublisher).publish(any(AppointmentEvent.class));
    }

    @Test
    void shouldRejectWhenCurrentUserIsNotDoctorOrNurse() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.create(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now().plusDays(1), "Inicial");
        appointment.setId(appointmentId);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(appointmentId, appointment.getPatientId(), appointment.getDoctorId(), LocalDateTime.now().plusDays(2), "Novo", UUID.randomUUID(), UserRole.ROLE_PATIENT));

        assertEquals("Apenas médicos e enfermeiros podem editar consultas", exception.getMessage());
    }

    @Test
    void shouldRejectDoctorEditingAnotherDoctorAppointment() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID currentDoctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Inicial");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(appointmentId, patientId, doctorId, LocalDateTime.now().plusDays(2), "Novo", currentDoctorId, UserRole.ROLE_DOCTOR));

        assertEquals("Médico só pode editar suas consultas", exception.getMessage());
    }

    @Test
    void shouldRejectWhenPatientDoesNotExistOnUpdate() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();

        Appointment appointment = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Inicial");
        appointment.setId(appointmentId);

        when(appointmentGateway.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(userGateway.findById(patientId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(appointmentId, patientId, doctorId, LocalDateTime.now().plusDays(2), "Novo", doctorId, UserRole.ROLE_DOCTOR));

        assertEquals("Paciente não encontrado", exception.getMessage());
    }
}
