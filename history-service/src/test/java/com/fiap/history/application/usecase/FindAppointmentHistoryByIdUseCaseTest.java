package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAppointmentHistoryByIdUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindAppointmentHistoryByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentHistoryByIdUseCase(appointmentHistoryGateway);
    }

    @Test
    void shouldReturnAppointmentHistoryById() {
        UUID id = UUID.randomUUID();
        AppointmentHistory expected = new AppointmentHistory();
        when(appointmentHistoryGateway.findById(id)).thenReturn(expected);

        AppointmentHistory result = useCase.execute(id);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findById(id);
    }
}
