package com.fiap.scheduling.domain.entity;

/**
 * Status de uma consulta médica.
 * Transições válidas:
 * SCHEDULED → CONFIRMED → COMPLETED
 * SCHEDULED → CANCELLED
 * CONFIRMED → CANCELLED
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
