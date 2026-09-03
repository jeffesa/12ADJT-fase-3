package com.fiap.history.infra.persistence;

import com.fiap.history.domain.entity.AppointmentHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import(AppointmentHistoryJpaGateway.class)
class AppointmentHistoryJpaGatewayTest {

    @Autowired
    private AppointmentHistoryJpaGateway gateway;

    private AppointmentHistory buildHistory(UUID patientId, UUID doctorId, LocalDateTime dateTime, String status, String eventType) {
        AppointmentHistory history = new AppointmentHistory();
        history.setId(UUID.randomUUID());
        history.setAppointmentId(UUID.randomUUID());
        history.setPatientId(patientId);
        history.setDoctorId(doctorId);
        history.setPatientName("Paciente Teste");
        history.setDoctorName("Médico Teste");
        history.setDateTime(dateTime);
        history.setStatus(status);
        history.setDescription("Consulta registrada");
        history.setEventType(eventType);
        history.setReceivedAt(LocalDateTime.now());
        return history;
    }

    @Test
    @DisplayName("Deve salvar um histórico de consulta")
    void shouldSave() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        AppointmentHistory saved = gateway.save(buildHistory(patientId, doctorId, LocalDateTime.now().plusDays(1), "SCHEDULED", "CREATED"));

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(patientId, saved.getPatientId());
        assertEquals(doctorId, saved.getDoctorId());
    }

    @Test
    @DisplayName("Deve buscar históricos por patientId")
    void shouldFindByPatientId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        gateway.save(buildHistory(patientId, doctorId, LocalDateTime.now().plusDays(1), "SCHEDULED", "CREATED"));
        gateway.save(buildHistory(patientId, UUID.randomUUID(), LocalDateTime.now().plusDays(2), "CONFIRMED", "UPDATED"));

        List<AppointmentHistory> result = gateway.findByPatientId(patientId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve buscar históricos por doctorId")
    void shouldFindByDoctorId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        gateway.save(buildHistory(patientId, doctorId, LocalDateTime.now().plusDays(1), "SCHEDULED", "CREATED"));
        gateway.save(buildHistory(UUID.randomUUID(), doctorId, LocalDateTime.now().plusDays(2), "CONFIRMED", "UPDATED"));

        List<AppointmentHistory> result = gateway.findByDoctorId(doctorId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve buscar consultas futuras do paciente")
    void shouldFindUpcomingByPatientId() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        gateway.save(buildHistory(patientId, doctorId, LocalDateTime.now().plusDays(3), "SCHEDULED", "CREATED"));
        gateway.save(buildHistory(patientId, doctorId, LocalDateTime.now().minusDays(1), "COMPLETED", "UPDATED"));

        List<AppointmentHistory> result = gateway.findUpcomingByPatientId(patientId);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getDateTime().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Deve retornar todos os históricos em ordem de recebimento")
    void shouldFindAll() {
        gateway.save(buildHistory(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now().plusDays(1), "SCHEDULED", "CREATED"));
        gateway.save(buildHistory(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now().plusDays(2), "CONFIRMED", "UPDATED"));

        List<AppointmentHistory> result = gateway.findAll();

        assertTrue(result.size() >= 2);
    }
}
