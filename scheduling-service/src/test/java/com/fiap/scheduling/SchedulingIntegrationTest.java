package com.fiap.scheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.scheduling.infra.persistence.AppointmentRepository;
import com.fiap.scheduling.infra.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de INTEGRAÇÃO real com @SpringBootTest + H2 (profile test).
 *
 * Exercita a stack completa (HTTP → SecurityFilter/JWT → Controller → UseCase →
 * Gateway → JPA → H2), SEM mockar use cases nem gateways. Confirma que os dados
 * realmente persistem e transitam por todas as camadas.
 *
 * RabbitMQ é excluído no profile test (RabbitAutoConfiguration off); o
 * EventPublisher no-op é usado, permitindo criar consultas sem broker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SchedulingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void cleanDatabase() {
        appointmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private JsonNode register(String name, String email, String role) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", name, "email", email, "password", "senha123", "role", role));
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String login(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", "senha123"));
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("Fluxo completo: registrar → login → criar consulta → buscar (persistindo em H2)")
    void fullFlowPersistsInH2() throws Exception {
        // registro real (persiste no H2)
        JsonNode doctor = register("Dr House", "house@hosp.com", "DOCTOR");
        JsonNode patient = register("Joao Paciente", "joao@mail.com", "PATIENT");
        String doctorId = doctor.get("userId").asText();
        String patientId = patient.get("userId").asText();

        // confirma persistência dos usuários no banco
        assertEquals(2, userRepository.count());

        // login real → token JWT
        String doctorToken = login("house@hosp.com");
        assertTrue(doctorToken.length() > 20);

        // criar consulta via HTTP (passa por security + use case + gateway + H2)
        String appointmentBody = objectMapper.writeValueAsString(Map.of(
                "patientId", patientId,
                "doctorId", doctorId,
                "dateTime", "2099-12-01T14:30:00",
                "description", "Consulta de rotina"));

        MvcResult created = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(appointmentBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn();

        String appointmentId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();

        // confirma persistência da consulta no banco
        assertEquals(1, appointmentRepository.count());

        // buscar a consulta criada via HTTP (retorna do H2)
        mockMvc.perform(get("/api/v1/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId))
                .andExpect(jsonPath("$.description").value("Consulta de rotina"));

        // listar todas via HTTP
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Integração de segurança: sem token → 403 na stack real")
    void unauthenticatedIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integração de autorização: PATIENT não cria consulta (403) na stack real")
    void patientCannotCreate() throws Exception {
        register("Pac", "pac@mail.com", "PATIENT");
        String patientToken = login("pac@mail.com");

        String body = objectMapper.writeValueAsString(Map.of(
                "patientId", java.util.UUID.randomUUID().toString(),
                "doctorId", java.util.UUID.randomUUID().toString(),
                "dateTime", "2099-12-01T14:30:00",
                "description", "x"));

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integração: email duplicado no registro → 422 na stack real")
    void duplicateEmailReturns422() throws Exception {
        register("User1", "dup@mail.com", "DOCTOR");

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "User2", "email", "dup@mail.com", "password", "senha123", "role", "NURSE"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }
}
