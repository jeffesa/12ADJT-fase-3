package com.fiap.notification.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de consulta recebido do scheduling-service via RabbitMQ.
 * Contrato compartilhado (mesma estrutura publicada pelo scheduling).
 */
public record AppointmentEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime dateTime,
        String status,
        String description,
        AppointmentEventType eventType
) {
}
