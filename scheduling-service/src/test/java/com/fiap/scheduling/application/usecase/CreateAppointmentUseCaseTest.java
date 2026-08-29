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
class CreateAppointmentUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private EventPublisher eventPublisher;

    private CreateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateAppointmentUseCase(appointmentGateway, userGateway, eventPublisher);
    }

    @Test
    void shouldCreateAppointmentWhenDoctorCreatesForSelf() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        User patient = new User(patientId, "Paciente", "paciente@email.com", "hash", UserRole.ROLE_PATIENT, LocalDateTime.now());
        User doctor = new User(doctorId, "Doutor", "medico@email.com", "hash", UserRole.ROLE_DOCTOR, LocalDateTime.now());

        when(userGateway.findById(patientId)).thenReturn(Optional.of(patient));
        when(userGateway.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentGateway.create(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(patientId, doctorId, futureDate, "Consulta de rotina", doctorId, UserRole.ROLE_DOCTOR);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals(futureDate, result.getDateTime());
        verify(eventPublisher).publish(any(AppointmentEvent.class));
    }

    @Test
    void shouldCreateAppointmentWhenNurseCreatesForDoctor() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        User patient = new User(patientId, "Paciente", "paciente@email.com", "hash", UserRole.ROLE_PATIENT, LocalDateTime.now());
        User doctor = new User(doctorId, "Doutor", "medico@email.com", "hash", UserRole.ROLE_DOCTOR, LocalDateTime.now());

        when(userGateway.findById(patientId)).thenReturn(Optional.of(patient));
        when(userGateway.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(appointmentGateway.create(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = useCase.execute(patientId, doctorId, futureDate, "Consulta", nurseId, UserRole.ROLE_NURSE);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals(doctorId, result.getDoctorId());
        verify(appointmentGateway).create(any(Appointment.class));
        verify(eventPublisher).publish(any(AppointmentEvent.class));
    }

    @Test
    void shouldRejectIfCurrentUserIsNotDoctorOrNurse() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta", UUID.randomUUID(), UserRole.ROLE_PATIENT));

        assertEquals("Apenas médicos e enfermeiros podem criar consultas", exception.getMessage());
        verifyNoInteractions(appointmentGateway, userGateway, eventPublisher);
    }

    @Test
    void shouldRejectDoctorCreatingForAnotherDoctor() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID currentDoctorId = UUID.randomUUID();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta", currentDoctorId, UserRole.ROLE_DOCTOR));

        assertEquals("Médico só pode agendar consultas para si", exception.getMessage());
        verifyNoInteractions(appointmentGateway, userGateway, eventPublisher);
    }

    @Test
    void shouldRejectWhenPatientDoesNotExist() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(userGateway.findById(patientId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta", doctorId, UserRole.ROLE_DOCTOR));

        assertEquals("Paciente não encontrado", exception.getMessage());
    }

    @Test
    void shouldRejectWhenDoctorDoesNotExist() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        User patient = new User(patientId, "Paciente", "paciente@email.com", "hash", UserRole.ROLE_PATIENT, LocalDateTime.now());

        when(userGateway.findById(patientId)).thenReturn(Optional.of(patient));
        when(userGateway.findById(doctorId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta", doctorId, UserRole.ROLE_DOCTOR));

        assertEquals("Médico não encontrado", exception.getMessage());
    }

    @Test
    void shouldRejectPastDateTime() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        User patient = new User(patientId, "Paciente", "paciente@email.com", "hash", UserRole.ROLE_PATIENT, LocalDateTime.now());
        User doctor = new User(doctorId, "Doutor", "medico@email.com", "hash", UserRole.ROLE_DOCTOR, LocalDateTime.now());

        when(userGateway.findById(patientId)).thenReturn(Optional.of(patient));
        when(userGateway.findById(doctorId)).thenReturn(Optional.of(doctor));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(patientId, doctorId, LocalDateTime.now().minusMinutes(1), "Consulta", doctorId, UserRole.ROLE_DOCTOR));
    }
}
