package com.fiap.scheduling.infra.messaging;

import com.fiap.scheduling.domain.event.EventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verificação de wiring da TASK-014 no contexto real (profile dev):
 * - o EventPublisher ativo deve ser o RabbitEventPublisher (não o no-op)
 * - deve existir exatamente um RabbitTemplate (sem ambiguidade)
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.datasource.url=jdbc:h2:mem:wiretest;DB_CLOSE_DELAY=-1"
})
class EventPublisherWiringVerificationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private org.springframework.context.ApplicationContext context;

    @Test
    void eventPublisherShouldBeRabbitImplementation() {
        assertThat(eventPublisher)
                .as("EventPublisher ativo deve ser o RabbitEventPublisher, não o no-op")
                .isInstanceOf(RabbitEventPublisher.class);
    }

    @Test
    void shouldHaveSingleRabbitTemplate() {
        String[] templates = context.getBeanNamesForType(RabbitTemplate.class);
        assertThat(templates)
                .as("Deve haver exatamente um RabbitTemplate (sem ambiguidade)")
                .hasSize(1);
    }
}
