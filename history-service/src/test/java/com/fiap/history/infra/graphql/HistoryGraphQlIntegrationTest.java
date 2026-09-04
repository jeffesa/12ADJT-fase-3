package com.fiap.history.infra.graphql;

import com.fiap.history.infra.persistence.AppointmentHistoryJpaEntity;
import com.fiap.history.infra.persistence.AppointmentHistoryRepository;
import com.fiap.history.infra.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
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

    /** Autentica o SecurityContext com o papel informado (ownership é validado nos resolvers). */
    private void authenticateAs(UUID userId, String role) {
        AuthenticatedUser principal = new AuthenticatedUser(userId, "user@test.com", role);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        // Padrão: enfermeiro (acesso amplo) para os cenários de sucesso
        authenticateAs(UUID.randomUUID(), "ROLE_NURSE");
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

    @Test
    void patientCanSeeOwnHistory() {
        // Paciente autenticado com o MESMO id do dado → autorizado
        authenticateAs(PATIENT_ID, "ROLE_PATIENT");

        graphQlTester.document("""
               query {
                 appointmentsByPatient(patientId: "00000000-0000-0000-0000-000000000001") {
                   status
                 }
               }
            """)
                .execute()
                .path("appointmentsByPatient[0].status").entity(String.class).isEqualTo("SCHEDULED");
    }

    @Test
    void patientCannotSeeOtherPatientHistory() {
        // Paciente autenticado com id DIFERENTE do solicitado → negado (erro GraphQL)
        authenticateAs(UUID.randomUUID(), "ROLE_PATIENT");

        graphQlTester.document("""
               query {
                 appointmentsByPatient(patientId: "00000000-0000-0000-0000-000000000001") {
                   status
                 }
               }
            """)
                .execute()
                .errors()
                .satisfy(errors -> org.assertj.core.api.Assertions.assertThat(errors).isNotEmpty());
    }
}
