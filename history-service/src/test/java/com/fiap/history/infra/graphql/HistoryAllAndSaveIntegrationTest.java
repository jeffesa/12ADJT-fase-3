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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class HistoryAllAndSaveIntegrationTest {

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
    void shouldReturnAllAppointments() {
        graphQlTester.document("""
               query {
                 allAppointmentHistories {
                   id
                   status
                 }
               }
            """)
                .execute()
                .path("allAppointmentHistories[0].status").entity(String.class).isEqualTo("SCHEDULED");
    }

    @Test
    void shouldSaveAppointmentHistory() {
        String mutation = "mutation($input: AppointmentHistoryInput!) { saveAppointmentHistory(input: $input) { id status patientId } }";

        graphQlTester.document(mutation)
                .variable("input", Map.of(
                        "appointmentId", UUID.randomUUID().toString(),
                        "patientId", PATIENT_ID.toString(),
                        "doctorId", DOCTOR_ID.toString(),
                        "patientName", "Joana",
                        "doctorName", "Dr. Test",
                        "dateTime", LocalDateTime.now().plusDays(2).toString(),
                        "status", "SCHEDULED",
                        "description", "Teste",
                        "eventType", "APPOINTMENT_CREATED"
                ))
                .execute()
                .path("saveAppointmentHistory.status").entity(String.class).isEqualTo("SCHEDULED");
    }
}
