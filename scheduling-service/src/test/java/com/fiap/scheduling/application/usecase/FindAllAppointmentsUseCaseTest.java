package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllAppointmentsUseCaseTest {

    @Mock
    private AppointmentGateway appointmentGateway;

    private FindAllAppointmentsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllAppointmentsUseCase(appointmentGateway);
    }

    private Appointment appointment(AppointmentStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new Appointment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                now.plusDays(1), status, "Consulta", now, now);
    }

    @Test
    @DisplayName("Deve retornar todas quando status não informado")
    void shouldReturnAll() {
        when(appointmentGateway.findAll()).thenReturn(List.of(
                appointment(AppointmentStatus.SCHEDULED),
                appointment(AppointmentStatus.CANCELLED)));

        List<Appointment> result = useCase.execute(null);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve filtrar por status quando informado")
    void shouldFilterByStatus() {
        when(appointmentGateway.findAll()).thenReturn(List.of(
                appointment(AppointmentStatus.SCHEDULED),
                appointment(AppointmentStatus.CANCELLED),
                appointment(AppointmentStatus.SCHEDULED)));

        List<Appointment> result = useCase.execute(AppointmentStatus.SCHEDULED);

        assertEquals(2, result.size());
        result.forEach(a -> assertEquals(AppointmentStatus.SCHEDULED, a.getStatus()));
    }
}
