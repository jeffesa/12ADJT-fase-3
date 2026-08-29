package com.fiap.scheduling.infra.config;

import com.fiap.scheduling.infra.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança do scheduling-service.
 * Sessão stateless com JWT, endpoints protegidos por role.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Appointments - criação e edição: DOCTOR e NURSE
                        .requestMatchers(HttpMethod.POST, "/api/v1/appointments").hasAnyRole("DOCTOR", "NURSE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/appointments/*").hasAnyRole("DOCTOR", "NURSE")
                        // Appointments - cancelamento: DOCTOR, NURSE ou PATIENT (ownership no use case)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/appointments/*/cancel")
                        .hasAnyRole("DOCTOR", "NURSE", "PATIENT")
                        // Appointments - listar por médico: DOCTOR e NURSE
                        .requestMatchers(HttpMethod.GET, "/api/v1/appointments/doctor/*").hasAnyRole("DOCTOR", "NURSE")
                        // Appointments - demais consultas: autenticado (ownership no use case)
                        .requestMatchers(HttpMethod.GET, "/api/v1/appointments/**").authenticated()
                        // Users - listar todos: DOCTOR e NURSE
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasAnyRole("DOCTOR", "NURSE")
                        // Users - buscar por id: autenticado (ownership do PATIENT no use case)
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/*").authenticated()
                        // Qualquer outra request: autenticada
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
