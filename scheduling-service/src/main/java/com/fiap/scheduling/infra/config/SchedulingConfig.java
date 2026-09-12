package com.fiap.scheduling.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita o suporte a @Scheduled. Desativado no profile 'test' para que o
 * scheduler de lembretes não dispare durante a execução dos testes.
 */
@Configuration
@Profile("!test")
@EnableScheduling
public class SchedulingConfig {
}
