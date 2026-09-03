package com.fiap.history.infra.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentHistoryEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime dateTime,
        String status,
        String eventType
) {
}
