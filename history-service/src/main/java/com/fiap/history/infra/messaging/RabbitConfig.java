package com.fiap.history.infra.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuração do RabbitMQ para o history-service (consumer).
 * Define exchange, queues, bindings, DLQ e política de retry.
 * Desabilitado no profile 'test' (RabbitAutoConfiguration é excluída lá).
 */
@Configuration
@Profile("!test")
public class RabbitConfig {

    // Exchange
    public static final String APPOINTMENT_EXCHANGE = "appointment.exchange";

    // History Queue
    public static final String HISTORY_QUEUE = "appointment.history.queue";
    public static final String HISTORY_DLQ = "appointment.history.dlq";

    // Routing Keys
    public static final String ROUTING_KEY_CREATED = "appointment.created";
    public static final String ROUTING_KEY_UPDATED = "appointment.updated";

    // Retry config
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_INTERVAL_MS = 1000;
    private static final double MULTIPLIER = 2.0;
    private static final long MAX_INTERVAL_MS = 10000;

    // DLQ TTL (24 horas em milissegundos)
    private static final int DLQ_TTL = 86_400_000;

    // ═══════════════════════════════════════
    // EXCHANGE
    // ═══════════════════════════════════════

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(APPOINTMENT_EXCHANGE);
    }

    // ═══════════════════════════════════════
    // QUEUES
    // ═══════════════════════════════════════

    @Bean
    public Queue historyQueue() {
        return QueueBuilder.durable(HISTORY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", HISTORY_DLQ)
                .build();
    }

    @Bean
    public Queue historyDlq() {
        return QueueBuilder.durable(HISTORY_DLQ)
                .withArgument("x-message-ttl", DLQ_TTL)
                .build();
    }

    // ═══════════════════════════════════════
    // BINDINGS
    // ═══════════════════════════════════════

    @Bean
    public Binding historyCreatedBinding(Queue historyQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(historyQueue)
                .to(appointmentExchange)
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding historyUpdatedBinding(Queue historyQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(historyQueue)
                .to(appointmentExchange)
                .with(ROUTING_KEY_UPDATED);
    }

    // ═══════════════════════════════════════
    // MESSAGE CONVERTER (JSON)
    // ═══════════════════════════════════════

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ═══════════════════════════════════════
    // RETRY POLICY + LISTENER CONTAINER
    // ═══════════════════════════════════════

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Exponential backoff: 1s → 2s → 4s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_INTERVAL_MS);
        backOffPolicy.setMultiplier(MULTIPLIER);
        backOffPolicy.setMaxInterval(MAX_INTERVAL_MS);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Max 3 tentativas
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(MAX_RETRY_ATTEMPTS);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jackson2JsonMessageConverter,
            RetryTemplate retryTemplate) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(10);

        // Configura retry com envio para DLQ após esgotadas as tentativas
        factory.setRetryTemplate(retryTemplate);

        // Mensagens rejeitadas vão para DLQ (não requeue)
        factory.setDefaultRequeueRejected(false);

        return factory;
    }
}
