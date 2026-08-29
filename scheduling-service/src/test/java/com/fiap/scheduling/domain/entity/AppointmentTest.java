package com.fiap.scheduling.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentTest {

    private final UUID patientId = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();

    @Test
    @DisplayName("create deve iniciar com status SCHEDULED e datas preenchidas")
    void createInitializesScheduled() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Consulta");

        assertNotNull(a.getId());
        assertEquals(AppointmentStatus.SCHEDULED, a.getStatus());
        assertNotNull(a.getCreatedAt());
        assertNotNull(a.getUpdatedAt());
    }

    @Test
    @DisplayName("create deve falhar quando dateTime não é futuro")
    void createFailsWhenNotFuture() {
        assertThrows(IllegalArgumentException.class,
                () -> Appointment.create(patientId, doctorId, LocalDateTime.now().minusDays(1), "Consulta"));
    }

    @Test
    @DisplayName("confirm: SCHEDULED → CONFIRMED")
    void confirmFromScheduled() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        a.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
    }

    @Test
    @DisplayName("complete: CONFIRMED → COMPLETED")
    void completeFromConfirmed() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        a.confirm();
        a.complete();
        assertEquals(AppointmentStatus.COMPLETED, a.getStatus());
    }

    @Test
    @DisplayName("complete direto de SCHEDULED deve falhar")
    void completeFromScheduledFails() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        assertThrows(IllegalArgumentException.class, a::complete);
    }

    @Test
    @DisplayName("cancel: SCHEDULED → CANCELLED")
    void cancelFromScheduled() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        a.cancel();
        assertEquals(AppointmentStatus.CANCELLED, a.getStatus());
    }

    @Test
    @DisplayName("não deve alterar consulta CANCELLED")
    void cannotChangeCancelled() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        a.cancel();
        assertThrows(IllegalArgumentException.class, a::confirm);
    }

    @Test
    @DisplayName("não deve alterar consulta COMPLETED")
    void cannotChangeCompleted() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        a.confirm();
        a.complete();
        assertThrows(IllegalArgumentException.class, a::cancel);
    }

    @Test
    @DisplayName("update deve alterar dateTime e description")
    void updateChangesFields() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "Antiga");
        LocalDateTime novaData = LocalDateTime.now().plusDays(5);

        a.update(novaData, "Nova descrição");

        assertEquals(novaData, a.getDateTime());
        assertEquals("Nova descrição", a.getDescription());
    }

    @Test
    @DisplayName("update com dateTime no passado deve falhar")
    void updateFailsWithPastDate() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        assertThrows(IllegalArgumentException.class,
                () -> a.update(LocalDateTime.now().minusDays(1), "x"));
    }

    @Test
    @DisplayName("update de consulta cancelada deve falhar")
    void updateCancelledFails() {
        Appointment a = Appointment.create(patientId, doctorId, LocalDateTime.now().plusDays(1), "C");
        a.cancel();
        assertThrows(IllegalArgumentException.class,
                () -> a.update(LocalDateTime.now().plusDays(2), "x"));
    }
}
