package com.fiap.history.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppointmentHistoryTest {

    @Test
    void shouldCreateWithNoArgsConstructorAndSetters() {
        AppointmentHistory history = new AppointmentHistory();
        UUID id = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        LocalDateTime receivedAt = LocalDateTime.now();

        history.setId(id);
        history.setAppointmentId(appointmentId);
        history.setPatientId(patientId);
        history.setDoctorId(doctorId);
        history.setPatientName("Paciente Teste");
        history.setDoctorName("Médico Teste");
        history.setDateTime(dateTime);
        history.setStatus("SCHEDULED");
        history.setDescription("Consulta de rotina");
        history.setEventType("CREATED");
        history.setReceivedAt(receivedAt);

        assertEquals(id, history.getId());
        assertEquals(appointmentId, history.getAppointmentId());
        assertEquals(patientId, history.getPatientId());
        assertEquals(doctorId, history.getDoctorId());
        assertEquals("Paciente Teste", history.getPatientName());
        assertEquals("Médico Teste", history.getDoctorName());
        assertEquals(dateTime, history.getDateTime());
        assertEquals("SCHEDULED", history.getStatus());
        assertEquals("Consulta de rotina", history.getDescription());
        assertEquals("CREATED", history.getEventType());
        assertEquals(receivedAt, history.getReceivedAt());
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
        LocalDateTime receivedAt = LocalDateTime.now();

        AppointmentHistory history = new AppointmentHistory(
                id,
                appointmentId,
                patientId,
                doctorId,
                "Paciente",
                "Médico",
                dateTime,
                "CONFIRMED",
                "Retorno",
                "UPDATED",
                receivedAt
        );

        assertEquals(id, history.getId());
        assertEquals(appointmentId, history.getAppointmentId());
        assertEquals(patientId, history.getPatientId());
        assertEquals(doctorId, history.getDoctorId());
        assertEquals("Paciente", history.getPatientName());
        assertEquals("Médico", history.getDoctorName());
        assertEquals(dateTime, history.getDateTime());
        assertEquals("CONFIRMED", history.getStatus());
        assertEquals("Retorno", history.getDescription());
        assertEquals("UPDATED", history.getEventType());
        assertEquals(receivedAt, history.getReceivedAt());
    }

    @Test
    void shouldStartWithNullFieldsWhenCreatedWithNoArgsConstructor() {
        AppointmentHistory history = new AppointmentHistory();

        assertNull(history.getId());
        assertNull(history.getAppointmentId());
        assertNull(history.getPatientId());
        assertNull(history.getDoctorId());
        assertNull(history.getPatientName());
        assertNull(history.getDoctorName());
        assertNull(history.getDateTime());
        assertNull(history.getStatus());
        assertNull(history.getDescription());
        assertNull(history.getEventType());
        assertNull(history.getReceivedAt());
    }
}
