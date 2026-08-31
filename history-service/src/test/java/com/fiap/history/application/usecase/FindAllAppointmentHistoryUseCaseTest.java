package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllAppointmentHistoryUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindAllAppointmentHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllAppointmentHistoryUseCase(appointmentHistoryGateway);
    }

    @Test
    void shouldReturnAllAppointmentHistory() {
        List<AppointmentHistory> expected = List.of(new AppointmentHistory(), new AppointmentHistory());
        when(appointmentHistoryGateway.findAll()).thenReturn(expected);

        List<AppointmentHistory> result = useCase.execute();

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findAll();
    }
}
