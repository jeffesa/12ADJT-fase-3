package com.fiap.notification.infra.web;

import com.fiap.notification.application.usecase.FindAllNotificationsUseCase;
import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.infra.config.SecurityConfig;
import com.fiap.notification.infra.security.AuthenticatedUser;
import com.fiap.notification.infra.security.JwtAuthenticationFilter;
import com.fiap.notification.infra.security.JwtTokenValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private FindAllNotificationsUseCase findAllNotificationsUseCase;
    @MockBean private JwtTokenValidator jwtTokenValidator;

    private static RequestPostProcessor authenticated() {
        AuthenticatedUser principal = new AuthenticatedUser(UUID.randomUUID(), "user@mail.com", "ROLE_DOCTOR");
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))));
    }

    private Notification sample(NotificationType type) {
        return new Notification(UUID.randomUUID(), "patient:123", "Consulta agendada",
                "corpo", type, LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /notifications autenticado → 200 e lista todas")
    void listAll() throws Exception {
        when(findAllNotificationsUseCase.execute(isNull()))
                .thenReturn(List.of(sample(NotificationType.APPOINTMENT_CREATED)));

        mockMvc.perform(get("/api/v1/notifications").with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Consulta agendada"))
                .andExpect(jsonPath("$[0].type").value("APPOINTMENT_CREATED"));
    }

    @Test
    @DisplayName("GET /notifications?type=APPOINTMENT_CREATED → 200 e filtra por tipo")
    void listFilteredByType() throws Exception {
        when(findAllNotificationsUseCase.execute(eq(NotificationType.APPOINTMENT_CREATED)))
                .thenReturn(List.of(sample(NotificationType.APPOINTMENT_CREATED)));

        mockMvc.perform(get("/api/v1/notifications?type=APPOINTMENT_CREATED").with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("APPOINTMENT_CREATED"));

        verify(findAllNotificationsUseCase).execute(NotificationType.APPOINTMENT_CREATED);
    }

    @Test
    @DisplayName("GET /notifications?type=INVALIDO → 422 (tipo inválido)")
    void invalidTypeReturns422() throws Exception {
        mockMvc.perform(get("/api/v1/notifications?type=INVALIDO").with(authenticated()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /notifications sem autenticação → 403")
    void unauthenticatedReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isForbidden());
    }
}
