package com.fiap.history.infra.graphql;

import com.fiap.history.infra.persistence.AppointmentHistoryJpaEntity;
import com.fiap.history.infra.persistence.AppointmentHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class HistoryGraphQlIntegrationTest {

    private static final UUID PATIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DOCTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private AppointmentHistoryRepository appointmentHistoryRepository;

    @BeforeEach
    void setUp() {
        appointmentHistoryRepository.deleteAll();

        AppointmentHistoryJpaEntity entity = new AppointmentHistoryJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setAppointmentId(UUID.randomUUID());
        entity.setPatientId(PATIENT_ID);
        entity.setDoctorId(DOCTOR_ID);
        entity.setPatientName("Maria Silva");
        entity.setDoctorName("Dr. João Pereira");
        entity.setDateTime(LocalDateTime.now().plusDays(3));
        entity.setStatus("SCHEDULED");
        entity.setDescription("Consulta de retorno");
        entity.setEventType("APPOINTMENT_CREATED");
        entity.setReceivedAt(LocalDateTime.now());

        appointmentHistoryRepository.save(entity);
    }

    @Test
    void shouldResolveAppointmentsByPatientQuery() {
        graphQlTester.document("""
               query {
                 appointmentsByPatient(patientId: "00000000-0000-0000-0000-000000000001") {
                   id
                   patientId
                   doctorId
                   status
                 }
               }
            """)
                .execute()
                .path("appointmentsByPatient[0].status").entity(String.class).isEqualTo("SCHEDULED");
    }

    @Test
    void shouldResolveAppointmentHistoryByIdQuery() {
        UUID historyId = appointmentHistoryRepository.findByPatientIdOrderByReceivedAtDesc(PATIENT_ID)
                .get(0)
                .getId();

        graphQlTester.document("""
               query {
                 appointmentHistory(id: "%s") {
                   id
                   patientId
                   doctorId
                   status
                 }
               }
            """.formatted(historyId))
                .execute()
                .path("appointmentHistory.status").entity(String.class).isEqualTo("SCHEDULED");
    }
}
