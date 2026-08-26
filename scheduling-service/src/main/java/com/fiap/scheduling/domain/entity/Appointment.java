package com.fiap.scheduling.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de domínio Appointment (POJO puro).
 * Representa uma consulta médica agendada.
 */
public class Appointment {

    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Appointment() {
    }

    public Appointment(UUID id, UUID patientId, UUID doctorId, LocalDateTime dateTime,
                       AppointmentStatus status, String description,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Appointment create(UUID patientId, UUID doctorId, LocalDateTime dateTime, String description) {
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.id = UUID.randomUUID();
        appointment.patientId = patientId;
        appointment.doctorId = doctorId;
        appointment.dateTime = dateTime;
        appointment.status = AppointmentStatus.SCHEDULED;
        appointment.description = description;
        appointment.createdAt = now;
        appointment.updatedAt = now;

        appointment.validateDateTimeInFuture();
        return appointment;
    }

    // ═══════════════════════════════════════
    // VALIDAÇÕES DE DOMÍNIO
    // ═══════════════════════════════════════

    public void validateDateTimeInFuture() {
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data/hora da consulta deve ser no futuro");
        }
    }

    public void validateStatusTransition(AppointmentStatus newStatus) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalArgumentException("Consulta cancelada não pode ser alterada");
        }
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Consulta finalizada não pode ser alterada");
        }

        switch (newStatus) {
            case CONFIRMED:
                if (this.status != AppointmentStatus.SCHEDULED) {
                    throw new IllegalArgumentException(
                            "Só é possível confirmar uma consulta com status SCHEDULED");
                }
                break;
            case COMPLETED:
                if (this.status != AppointmentStatus.CONFIRMED) {
                    throw new IllegalArgumentException(
                            "Só é possível completar uma consulta com status CONFIRMED");
                }
                break;
            case CANCELLED:
                // Pode cancelar de SCHEDULED ou CONFIRMED
                break;
            case SCHEDULED:
                throw new IllegalArgumentException("Não é possível voltar ao status SCHEDULED");
            default:
                throw new IllegalArgumentException("Status inválido: " + newStatus);
        }
    }

    // ═══════════════════════════════════════
    // MÉTODOS DE DOMÍNIO
    // ═══════════════════════════════════════

    public void confirm() {
        validateStatusTransition(AppointmentStatus.CONFIRMED);
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        validateStatusTransition(AppointmentStatus.CANCELLED);
        this.status = AppointmentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        validateStatusTransition(AppointmentStatus.COMPLETED);
        this.status = AppointmentStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(LocalDateTime newDateTime, String newDescription) {
        if (this.status == AppointmentStatus.CANCELLED || this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Consulta cancelada ou finalizada não pode ser editada");
        }
        if (newDateTime != null) {
            this.dateTime = newDateTime;
            validateDateTimeInFuture();
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════
    // GETTERS E SETTERS
    // ═══════════════════════════════════════

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
