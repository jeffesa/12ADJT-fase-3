package com.fiap.scheduling.infra.web.dto;

import com.fiap.scheduling.domain.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta para consulta.
 */
@Schema(description = "Dados de uma consulta")
public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        LocalDateTime dateTime,
        String status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AppointmentResponse fromDomain(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime(),
                appointment.getStatus().name(),
                appointment.getDescription(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
