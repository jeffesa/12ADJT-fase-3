package com.fiap.history.infra.messaging;

import com.fiap.history.application.usecase.SaveAppointmentHistoryUseCase;
import com.fiap.history.domain.entity.AppointmentHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentHistoryListenerTest {

    @Mock
    private SaveAppointmentHistoryUseCase saveUseCase;

    private AppointmentHistoryListener listener;

    @BeforeEach
    void setUp() {
        listener = new AppointmentHistoryListener(saveUseCase);
    }

    @Test
    void shouldNotFailWhenEventIsNull() {
        // should simply return without throwing
        listener.listen(null);
        verifyNoInteractions(saveUseCase);
    }

    @Test
    void shouldMapEventAndCallSaveUseCase() {
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        String status = "SCHEDULED";
        String eventType = "APPOINTMENT_CREATED";

        AppointmentHistoryEvent event = new AppointmentHistoryEvent(
                appointmentId,
                patientId,
                doctorId,
                dateTime,
                status,
                eventType
        );

        listener.listen(event);

        ArgumentCaptor<AppointmentHistory> captor = ArgumentCaptor.forClass(AppointmentHistory.class);
        verify(saveUseCase, times(1)).execute(captor.capture());

        AppointmentHistory captured = captor.getValue();
        assertEquals(appointmentId, captured.getAppointmentId());
        assertEquals(patientId, captured.getPatientId());
        assertEquals(doctorId, captured.getDoctorId());
        assertEquals(status, captured.getStatus());
        assertEquals(eventType, captured.getEventType());
        assertNotNull(captured.getReceivedAt());
    }
}
