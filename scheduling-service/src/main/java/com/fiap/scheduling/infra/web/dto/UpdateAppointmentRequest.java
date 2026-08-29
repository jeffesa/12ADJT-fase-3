package com.fiap.scheduling.infra.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para atualização de consulta.
 * Campos opcionais — apenas os informados serão atualizados.
 */
@Schema(description = "Dados para atualização de uma consulta")
public record UpdateAppointmentRequest(

        @Schema(description = "Novo ID do paciente (opcional)")
        UUID patientId,

        @Schema(description = "Novo ID do médico (opcional)")
        UUID doctorId,

        @Schema(description = "Nova data e hora (opcional, deve ser no futuro)", example = "2026-12-02T10:00:00")
        @Future(message = "dateTime deve ser no futuro")
        LocalDateTime dateTime,

        @Schema(description = "Nova descrição (opcional)")
        String description
) {
}
