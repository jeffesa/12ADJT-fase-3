package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveAppointmentHistoryUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private SaveAppointmentHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SaveAppointmentHistoryUseCase(appointmentHistoryGateway);
    }

    @Test
    void shouldSaveAppointmentHistory() {
        AppointmentHistory input = new AppointmentHistory();
        AppointmentHistory saved = new AppointmentHistory();
        when(appointmentHistoryGateway.save(input)).thenReturn(saved);

        AppointmentHistory result = useCase.execute(input);

        assertEquals(saved, result);
        verify(appointmentHistoryGateway).save(input);
    }
}
