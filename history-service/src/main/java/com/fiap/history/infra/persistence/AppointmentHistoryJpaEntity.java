package com.fiap.history.infra.persistence;

import com.fiap.history.domain.entity.AppointmentHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_history")
public class AppointmentHistoryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public AppointmentHistoryJpaEntity() {
    }

    public static AppointmentHistoryJpaEntity fromDomain(AppointmentHistory appointmentHistory) {
        AppointmentHistoryJpaEntity entity = new AppointmentHistoryJpaEntity();
        entity.setId(appointmentHistory.getId() != null ? appointmentHistory.getId() : UUID.randomUUID());
        entity.setAppointmentId(appointmentHistory.getAppointmentId());
        entity.setPatientId(appointmentHistory.getPatientId());
        entity.setDoctorId(appointmentHistory.getDoctorId());
        entity.setPatientName(appointmentHistory.getPatientName());
        entity.setDoctorName(appointmentHistory.getDoctorName());
        entity.setDateTime(appointmentHistory.getDateTime());
        entity.setStatus(appointmentHistory.getStatus());
        entity.setDescription(appointmentHistory.getDescription());
        entity.setEventType(appointmentHistory.getEventType());
        entity.setReceivedAt(appointmentHistory.getReceivedAt());
        return entity;
    }

    public AppointmentHistory toDomain() {
        return new AppointmentHistory(
                this.id,
                this.appointmentId,
                this.patientId,
                this.doctorId,
                this.patientName,
                this.doctorName,
                this.dateTime,
                this.status,
                this.description,
                this.eventType,
                this.receivedAt
        );
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
