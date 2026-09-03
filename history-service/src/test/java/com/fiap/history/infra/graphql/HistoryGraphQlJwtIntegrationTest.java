package com.fiap.history.infra.graphql;

import com.fiap.history.infra.persistence.AppointmentHistoryJpaEntity;
import com.fiap.history.infra.persistence.AppointmentHistoryRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HistoryGraphQlJwtIntegrationTest {

    private static final UUID PATIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DOCTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private TestRestTemplate restTemplate;

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
    void graphqlShouldReturnOkWithRealJwt() {
        // secret base64 defined in application.yml default
        String secretBase64 = "dGVjaC1jaGFsbGVuZ2UtZmFzZTMtand0LXNlY3JldC1rZXktMjAyNi1maWFwLXByb2plY3Q=";
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);

        UUID userId = PATIENT_ID;

        String token = Jwts.builder()
                .setSubject("user@example.com")
                .claim("role", "ROLE_PATIENT")
                .claim("userId", userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(java.time.Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();

        String payload = "{\"query\": \"query { appointmentsByPatient(patientId: \"00000000-0000-0000-0000-000000000001\") { id status patientId } }\" }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/graphql", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("SCHEDULED");
    }
}
