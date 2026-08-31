package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUpcomingAppointmentHistoryByPatientIdUseCaseTest {

    @Mock
    private AppointmentHistoryGateway appointmentHistoryGateway;

    private FindUpcomingAppointmentHistoryByPatientIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindUpcomingAppointmentHistoryByPatientIdUseCase(appointmentHistoryGateway);
    }

    @Test
    void shouldReturnUpcomingAppointmentHistoryByPatientId() {
        UUID patientId = UUID.randomUUID();
        List<AppointmentHistory> expected = List.of(new AppointmentHistory());
        when(appointmentHistoryGateway.findUpcomingByPatientId(patientId)).thenReturn(expected);

        List<AppointmentHistory> result = useCase.execute(patientId);

        assertEquals(expected, result);
        verify(appointmentHistoryGateway).findUpcomingByPatientId(patientId);
    }
}
