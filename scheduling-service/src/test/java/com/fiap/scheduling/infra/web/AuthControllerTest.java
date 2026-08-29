package com.fiap.scheduling.infra.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.scheduling.application.usecase.LoginUseCase;
import com.fiap.scheduling.application.usecase.RegisterUserUseCase;
import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.TokenProvider;
import com.fiap.scheduling.infra.config.SecurityConfig;
import com.fiap.scheduling.infra.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private TokenProvider tokenProvider;
    @MockBean private RegisterUserUseCase registerUserUseCase;
    @MockBean private LoginUseCase loginUseCase;

    private User sampleUser() {
        return new User(UUID.randomUUID(), "João", "joao@mail.com", "hash",
                UserRole.ROLE_DOCTOR, LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /auth/register (público) → 201 com token")
    void registerReturns201() throws Exception {
        User user = sampleUser();
        when(registerUserUseCase.execute(any(), any(), any(), any())).thenReturn(user);
        when(tokenProvider.generateToken(eq(user.getId()), eq(user.getEmail()), eq("ROLE_DOCTOR")))
                .thenReturn("jwt-token");

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "João", "email", "joao@mail.com", "password", "senha123", "role", "DOCTOR"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("ROLE_DOCTOR"));
    }

    @Test
    @DisplayName("POST /auth/register com body inválido → 400")
    void registerInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "", "email", "invalido"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login (público) → 200 com token")
    void loginReturns200() throws Exception {
        User user = sampleUser();
        when(loginUseCase.execute(eq("joao@mail.com"), eq("senha123")))
                .thenReturn(new LoginUseCase.LoginResult("jwt-token", user));

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "joao@mail.com", "password", "senha123"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/login com credenciais inválidas → 422")
    void loginInvalidCredentials() throws Exception {
        when(loginUseCase.execute(any(), any()))
                .thenThrow(new BusinessException("Email ou senha inválidos"));

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "joao@mail.com", "password", "errada"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }
}
