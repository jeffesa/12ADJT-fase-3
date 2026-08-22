package com.fiap.scheduling.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc OpenAPI para o scheduling-service.
 * Swagger UI acessível em /swagger-ui.html
 * Suporte a Bearer JWT no Swagger.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scheduling Service API")
                        .description("API de Agendamento de Consultas - Sistema Hospitalar (Tech Challenge Fase 3)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FIAP - Grupo 12ADJT")
                                .email("jefferson@fiap.com.br")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint /api/v1/auth/login")));
    }
}
