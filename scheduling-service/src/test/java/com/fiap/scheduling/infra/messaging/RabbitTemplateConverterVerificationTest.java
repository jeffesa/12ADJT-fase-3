package com.fiap.scheduling.infra.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de verificação (temporário) — sobe o contexto real da aplicação no
 * profile dev (com RabbitAutoConfiguration ativa) e confirma que o RabbitTemplate
 * usa o Jackson2JsonMessageConverter, garantindo publicação em JSON.
 *
 * Não conecta ao RabbitMQ de fato (apenas inspeciona o bean configurado).
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.datasource.url=jdbc:h2:mem:convtest;DB_CLOSE_DELAY=-1"
})
class RabbitTemplateConverterVerificationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void rabbitTemplateShouldUseJacksonConverter() {
        assertThat(rabbitTemplate.getMessageConverter())
                .as("RabbitTemplate deve publicar em JSON (Jackson2JsonMessageConverter)")
                .isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
