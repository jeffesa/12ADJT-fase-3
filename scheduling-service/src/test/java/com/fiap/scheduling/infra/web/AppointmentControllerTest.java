package com.fiap.scheduling.infra.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.scheduling.application.usecase.CancelAppointmentUseCase;
import com.fiap.scheduling.application.usecase.CreateAppointmentUseCase;
import com.fiap.scheduling.application.usecase.FindAllAppointmentsUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentByIdUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentsByDoctorUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentsByPatientUseCase;
import com.fiap.scheduling.application.usecase.FindUpcomingAppointmentsUseCase;
import com.fiap.scheduling.application.usecase.UpdateAppointmentUseCase;
import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.infra.config.SecurityConfig;
import com.fiap.scheduling.infra.security.AuthenticatedUser;
import com.fiap.scheduling.infra.security.JwtAuthenticationFilter;
import com.fiap.scheduling.domain.shared.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Dependências dos beans importados / do controller
    @MockBean private TokenProvider tokenProvider;
    @MockBean private CreateAppointmentUseCase createAppointmentUseCase;
    @MockBean private UpdateAppointmentUseCase updateAppointmentUseCase;
    @MockBean private CancelAppointmentUseCase cancelAppointmentUseCase;
    @MockBean private FindAppointmentByIdUseCase findAppointmentByIdUseCase;
    @MockBean private FindAppointmentsByPatientUseCase findAppointmentsByPatientUseCase;
    @MockBean private FindAppointmentsByDoctorUseCase findAppointmentsByDoctorUseCase;
    @MockBean private FindUpcomingAppointmentsUseCase findUpcomingAppointmentsUseCase;
    @MockBean private FindAllAppointmentsUseCase findAllAppointmentsUseCase;

    private static RequestPostProcessor asUser(UserRole role) {
        AuthenticatedUser principal = new AuthenticatedUser(UUID.randomUUID(), "user@mail.com", role.name());
        return authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role.name()))));
    }

    private Appointment sampleAppointment() {
        LocalDateTime now = LocalDateTime.now();
        return new Appointment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                now.plusDays(1), AppointmentStatus.SCHEDULED, "Consulta", now, now);
    }

    private String createBody() throws Exception {
        return objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("patientId", UUID.randomUUID().toString());
            put("doctorId", UUID.randomUUID().toString());
            put("dateTime", "2099-12-01T14:30:00");
            put("description", "Consulta de rotina");
        }});
    }

    @Test
    @DisplayName("POST /appointments sem autenticação → 403")
    void createUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /appointments como PATIENT → 403")
    void createAsPatientForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .with(asUser(UserRole.ROLE_PATIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /appointments como DOCTOR → 201")
    void createAsDoctor() throws Exception {
        when(createAppointmentUseCase.execute(any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleAppointment());

        mockMvc.perform(post("/api/v1/appointments")
                        .with(asUser(UserRole.ROLE_DOCTOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /appointments como NURSE → 201")
    void createAsNurse() throws Exception {
        when(createAppointmentUseCase.execute(any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleAppointment());

        mockMvc.perform(post("/api/v1/appointments")
                        .with(asUser(UserRole.ROLE_NURSE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /appointments com body inválido (DOCTOR) → 400")
    void createInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .with(asUser(UserRole.ROLE_DOCTOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"faltando campos\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /appointments (listar todas) como DOCTOR → 200")
    void listAllAsDoctor() throws Exception {
        when(findAllAppointmentsUseCase.execute(any())).thenReturn(List.of(sampleAppointment()));

        mockMvc.perform(get("/api/v1/appointments").with(asUser(UserRole.ROLE_DOCTOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /appointments (listar todas) como PATIENT → 403")
    void listAllAsPatientForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/appointments").with(asUser(UserRole.ROLE_PATIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /appointments/doctor/{id} como PATIENT → 403")
    void listByDoctorAsPatientForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/doctor/" + UUID.randomUUID())
                        .with(asUser(UserRole.ROLE_PATIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /appointments/upcoming como PATIENT autenticado → 200")
    void upcomingAsPatient() throws Exception {
        when(findUpcomingAppointmentsUseCase.execute(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/appointments/upcoming").with(asUser(UserRole.ROLE_PATIENT)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments/{id} autenticado → 200")
    void findByIdAuthenticated() throws Exception {
        when(findAppointmentByIdUseCase.execute(any(), any(), any())).thenReturn(sampleAppointment());

        mockMvc.perform(get("/api/v1/appointments/" + UUID.randomUUID())
                        .with(asUser(UserRole.ROLE_PATIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /appointments/{id} sem autenticação → 403")
    void findByIdUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /appointments/patient/{id} como DOCTOR → 200")
    void findByPatientAsDoctor() throws Exception {
        when(findAppointmentsByPatientUseCase.execute(any(), any(), any()))
                .thenReturn(List.of(sampleAppointment()));

        mockMvc.perform(get("/api/v1/appointments/patient/" + UUID.randomUUID())
                        .with(asUser(UserRole.ROLE_DOCTOR)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments/doctor/{id} como NURSE → 200")
    void findByDoctorAsNurse() throws Exception {
        when(findAppointmentsByDoctorUseCase.execute(any(), any(), any()))
                .thenReturn(List.of(sampleAppointment()));

        mockMvc.perform(get("/api/v1/appointments/doctor/" + UUID.randomUUID())
                        .with(asUser(UserRole.ROLE_NURSE)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /appointments/{id} como DOCTOR → 200")
    void updateAsDoctor() throws Exception {
        when(updateAppointmentUseCase.execute(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleAppointment());

        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("description", "Atualizada");
        }});

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/appointments/" + UUID.randomUUID())
                        .with(asUser(UserRole.ROLE_DOCTOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/cancel como PATIENT → 200")
    void cancelAsPatient() throws Exception {
        when(cancelAppointmentUseCase.execute(any(), any(), any())).thenReturn(sampleAppointment());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/appointments/" + UUID.randomUUID() + "/cancel")
                        .with(asUser(UserRole.ROLE_PATIENT)))
                .andExpect(status().isOk());
    }
}
