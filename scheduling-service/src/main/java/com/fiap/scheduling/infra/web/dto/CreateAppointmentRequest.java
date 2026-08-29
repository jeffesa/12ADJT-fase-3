package com.fiap.scheduling.infra.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para criação de consulta.
 */
@Schema(description = "Dados para criação de uma consulta")
public record CreateAppointmentRequest(

        @Schema(description = "ID do paciente", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @NotNull(message = "patientId é obrigatório")
        UUID patientId,

        @Schema(description = "ID do médico", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        @NotNull(message = "doctorId é obrigatório")
        UUID doctorId,

        @Schema(description = "Data e hora da consulta (deve ser no futuro)", example = "2026-12-01T14:30:00")
        @NotNull(message = "dateTime é obrigatório")
        @Future(message = "dateTime deve ser no futuro")
        LocalDateTime dateTime,

        @Schema(description = "Descrição da consulta", example = "Consulta de rotina")
        @NotBlank(message = "description é obrigatória")
        String description
) {
}
