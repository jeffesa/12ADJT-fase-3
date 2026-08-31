package com.fiap.history.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentHistory {

    private UUID id;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private String patientName;
    private String doctorName;
    private LocalDateTime dateTime;
    private String status;
    private String description;
    private String eventType;
    private LocalDateTime receivedAt;

    public AppointmentHistory() {
    }

    public AppointmentHistory(UUID id,
                              UUID appointmentId,
                              UUID patientId,
                              UUID doctorId,
                              String patientName,
                              String doctorName,
                              LocalDateTime dateTime,
                              String status,
                              String description,
                              String eventType,
                              LocalDateTime receivedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.dateTime = dateTime;
        this.status = status;
        this.description = description;
        this.eventType = eventType;
        this.receivedAt = receivedAt;
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
