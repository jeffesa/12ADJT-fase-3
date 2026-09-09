package com.fiap.scheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.scheduling.infra.persistence.AppointmentRepository;
import com.fiap.scheduling.infra.persistence.UserRepository;
import com.fiap.scheduling.infra.security.JwtTokenProvider;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes dedicados de segurança (TASK-030) — autenticação e autorização.
 *
 * Convenção HTTP aplicada:
 *  - 401 Unauthorized: não autenticado (sem token, token inválido, token expirado)
 *  - 403 Forbidden: autenticado mas sem permissão (role insuficiente ou ownership)
 *
 * Exercita a stack HTTP real (@SpringBootTest + MockMvc), com JWT real gerado via
 * register/login e tokens forjados (inválido/expirado) via JwtTokenProvider.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    // Mesma secret default do application.yml (perfil test) — tokens gerados aqui
    // são aceitos pelo filtro real.
    private static final String SECRET = "dGVjaC1jaGFsbGVuZ2UtZmFzZTMtand0LXNlY3JldC1rZXktMjAyNi1maWFwLXByb2plY3Q=";

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

    private String createBody(String patientId, String doctorId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "patientId", patientId,
                "doctorId", doctorId,
                "dateTime", "2099-12-01T14:30:00",
                "description", "Consulta"));
    }

    // ─── Autenticação (401) ──────────────────────────────────────

    @Test
    @DisplayName("Sem token → 401")
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token inválido (malformado) → 401")
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer token-invalido-xyz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token expirado → 401")
    void expiredTokenReturns401() throws Exception {
        // Gera um token válido em estrutura, mas já expirado (expiração de 1ms).
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, 1L);
        String expired = expiredProvider.generateToken(
                UUID.randomUUID(), "expired@fiap.com", "ROLE_DOCTOR");
        Thread.sleep(50); // garante que passou da expiração

        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    // ─── Autorização (403) ───────────────────────────────────────

    @Test
    @DisplayName("PATIENT tentando criar consulta → 403")
    void patientCreatingAppointmentReturns403() throws Exception {
        register("Pac", "pac@mail.com", "PATIENT");
        String patientToken = login("pac@mail.com");

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(UUID.randomUUID().toString(), UUID.randomUUID().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATIENT vendo consulta de outro paciente → 403 (ownership)")
    void patientSeeingOtherPatientAppointmentReturns403() throws Exception {
        JsonNode doctor = register("Dr X", "drx@hosp.com", "DOCTOR");
        JsonNode owner = register("Dono", "dono@mail.com", "PATIENT");
        register("Outro", "outro@mail.com", "PATIENT");

        String doctorToken = login("drx@hosp.com");
        String otherToken = login("outro@mail.com");

        MvcResult created = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(owner.get("userId").asText(), doctor.get("userId").asText())))
                .andExpect(status().isCreated())
                .andReturn();
        String appointmentId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/api/v1/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    // ─── Sucesso (201) ───────────────────────────────────────────

    @Test
    @DisplayName("DOCTOR criando consulta → 201")
    void doctorCreatingAppointmentReturns201() throws Exception {
        JsonNode doctor = register("Dr House", "house@hosp.com", "DOCTOR");
        JsonNode patient = register("Joao", "joao@mail.com", "PATIENT");
        String doctorToken = login("house@hosp.com");

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(patient.get("userId").asText(), doctor.get("userId").asText())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("NURSE registrando consulta → 201")
    void nurseCreatingAppointmentReturns201() throws Exception {
        // A NURSE pode registrar a consulta (autorização), mas o doctorId
        // precisa apontar para um usuário com papel DOCTOR (regra de negócio).
        register("Enf Ana", "ana@hosp.com", "NURSE");
        JsonNode doctor = register("Dr Bob", "bob@hosp.com", "DOCTOR");
        JsonNode patient = register("Maria", "maria@mail.com", "PATIENT");
        String nurseToken = login("ana@hosp.com");

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer " + nurseToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(patient.get("userId").asText(), doctor.get("userId").asText())))
                .andExpect(status().isCreated());
    }
}
