package com.fiap.scheduling.infra.web;

import com.fiap.scheduling.application.usecase.FindAllUsersUseCase;
import com.fiap.scheduling.application.usecase.FindUserByIdUseCase;
import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.shared.TokenProvider;
import com.fiap.scheduling.infra.config.SecurityConfig;
import com.fiap.scheduling.infra.security.AuthenticatedUser;
import com.fiap.scheduling.infra.security.JwtAuthenticationFilter;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private TokenProvider tokenProvider;
    @MockBean private FindAllUsersUseCase findAllUsersUseCase;
    @MockBean private FindUserByIdUseCase findUserByIdUseCase;

    private static final UUID SELF_ID = UUID.randomUUID();

    private static RequestPostProcessor asUser(UUID id, UserRole role) {
        AuthenticatedUser principal = new AuthenticatedUser(id, "user@mail.com", role.name());
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role.name()))));
    }

    private User sampleUser(UUID id, UserRole role) {
        return new User(id, "Nome", "e@mail.com", "hash", role, LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /users como DOCTOR → 200 e não expõe senha")
    void listAsDoctor() throws Exception {
        when(findAllUsersUseCase.execute(any()))
                .thenReturn(List.of(sampleUser(UUID.randomUUID(), UserRole.ROLE_PATIENT)));

        mockMvc.perform(get("/api/v1/users").with(asUser(UUID.randomUUID(), UserRole.ROLE_DOCTOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("e@mail.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("GET /users como NURSE → 200")
    void listAsNurse() throws Exception {
        when(findAllUsersUseCase.execute(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users").with(asUser(UUID.randomUUID(), UserRole.ROLE_NURSE)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users como PATIENT → 403")
    void listAsPatientForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(asUser(UUID.randomUUID(), UserRole.ROLE_PATIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /users sem autenticação → 403")
    void listUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /users/{id} como PATIENT (autenticado) → 200")
    void findByIdAsPatient() throws Exception {
        when(findUserByIdUseCase.execute(any(), any(), any()))
                .thenReturn(sampleUser(SELF_ID, UserRole.ROLE_PATIENT));

        mockMvc.perform(get("/api/v1/users/" + SELF_ID).with(asUser(SELF_ID, UserRole.ROLE_PATIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
