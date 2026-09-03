package com.fiap.history.infra.graphql;

import com.fiap.history.domain.entity.AppointmentHistory;

import java.util.UUID;

public class AppointmentHistoryGraphql {

    private UUID id;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private String patientName;
    private String doctorName;
    private String dateTime;
    private String status;
    private String description;
    private String eventType;
    private String receivedAt;

    public static AppointmentHistoryGraphql fromDomain(AppointmentHistory appointmentHistory) {
        AppointmentHistoryGraphql dto = new AppointmentHistoryGraphql();
        dto.setId(appointmentHistory.getId());
        dto.setAppointmentId(appointmentHistory.getAppointmentId());
        dto.setPatientId(appointmentHistory.getPatientId());
        dto.setDoctorId(appointmentHistory.getDoctorId());
        dto.setPatientName(appointmentHistory.getPatientName());
        dto.setDoctorName(appointmentHistory.getDoctorName());
        dto.setDateTime(appointmentHistory.getDateTime() != null ? appointmentHistory.getDateTime().toString() : null);
        dto.setStatus(appointmentHistory.getStatus());
        dto.setDescription(appointmentHistory.getDescription());
        dto.setEventType(appointmentHistory.getEventType());
        dto.setReceivedAt(appointmentHistory.getReceivedAt() != null ? appointmentHistory.getReceivedAt().toString() : null);
        return dto;
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
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

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}
