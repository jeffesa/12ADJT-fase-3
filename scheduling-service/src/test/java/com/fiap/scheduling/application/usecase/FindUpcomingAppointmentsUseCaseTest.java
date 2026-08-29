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
class FindUpcomingAppointmentsUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private FindUpcomingAppointmentsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindUpcomingAppointmentsUseCase(appointmentGateway);
    }

    @Test
    void shouldReturnOnlyOwnUpcomingAppointmentsForPatient() {
        UUID patientId = UUID.randomUUID();
        UUID otherPatientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        Appointment own = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Minha consulta");
        Appointment other = Appointment.create(otherPatientId, doctorId, LocalDateTime.now().plusDays(2), "Outra consulta");

        when(appointmentGateway.findUpcoming(any(LocalDateTime.class))).thenReturn(List.of(own, other));

        List<Appointment> result = useCase.execute(patientId, UserRole.ROLE_PATIENT);

        assertEquals(1, result.size());
        assertEquals(patientId, result.get(0).getPatientId());
    }

    @Test
    void shouldReturnOnlyOwnUpcomingAppointmentsForDoctor() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID otherDoctorId = UUID.randomUUID();

        Appointment own = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Minha consulta");
        Appointment other = Appointment.create(UUID.randomUUID(), otherDoctorId, LocalDateTime.now().plusDays(2), "Outra consulta");

        when(appointmentGateway.findUpcoming(any(LocalDateTime.class))).thenReturn(List.of(own, other));

        List<Appointment> result = useCase.execute(doctorId, UserRole.ROLE_DOCTOR);

        assertEquals(1, result.size());
        assertEquals(doctorId, result.get(0).getDoctorId());
    }

    @Test
    void shouldReturnAllUpcomingAppointmentsForNurse() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID otherDoctorId = UUID.randomUUID();

        Appointment first = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta 1");
        Appointment second = Appointment.create(UUID.randomUUID(), otherDoctorId, LocalDateTime.now().plusDays(2), "Consulta 2");

        when(appointmentGateway.findUpcoming(any(LocalDateTime.class))).thenReturn(List.of(first, second));

        List<Appointment> result = useCase.execute(UUID.randomUUID(), UserRole.ROLE_NURSE);

        assertEquals(2, result.size());
    }

    @Test
    void shouldRejectIfUserIsNull() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> useCase.execute(UUID.randomUUID(), null));

        assertEquals("Usuário não autenticado", exception.getMessage());
    }
}
