package com.fiap.notification.infra.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verificação de wiring do RabbitConfig no contexto real (profile dev):
 * confirma que a config Rabbit é carregada (exchange + listener factory presentes),
 * garantindo que o @Profile("!test") funciona onde o @ConditionalOnBean falhava.
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.rabbitmq.host=localhost")
class RabbitConfigWiringVerificationTest {

    @Autowired
    private org.springframework.context.ApplicationContext context;

    @Test
    void rabbitConfigBeansShouldBePresentInDevProfile() {
        assertThat(context.getBeanNamesForType(TopicExchange.class))
                .as("appointmentExchange deve estar presente")
                .isNotEmpty();
        assertThat(context.getBeanNamesForType(SimpleRabbitListenerContainerFactory.class))
                .as("rabbitListenerContainerFactory (JSON + retry) deve estar presente")
                .isNotEmpty();
    }
}
