package com.fiap.scheduling.infra.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração do RabbitMQ para o scheduling-service (publisher).
 * Define exchange, queues, bindings e DLQs para comunicação assíncrona.
 * Desabilitado no profile 'test' (RabbitAutoConfiguration é excluída lá).
 */
@Configuration
@Profile("!test")
public class RabbitConfig {

    // Exchange
    public static final String APPOINTMENT_EXCHANGE = "appointment.exchange";

    // Notification Queue
    public static final String NOTIFICATION_QUEUE = "appointment.notification.queue";
    public static final String NOTIFICATION_DLQ = "appointment.notification.dlq";

    // History Queue
    public static final String HISTORY_QUEUE = "appointment.history.queue";
    public static final String HISTORY_DLQ = "appointment.history.dlq";

    // Routing Keys
    public static final String ROUTING_KEY_CREATED = "appointment.created";
    public static final String ROUTING_KEY_UPDATED = "appointment.updated";

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
    // NOTIFICATION QUEUES
    // ═══════════════════════════════════════

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ)
                .withArgument("x-message-ttl", DLQ_TTL)
                .build();
    }

    // ═══════════════════════════════════════
    // HISTORY QUEUES
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
    // BINDINGS - NOTIFICATION
    // ═══════════════════════════════════════

    @Bean
    public Binding notificationCreatedBinding(Queue notificationQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(appointmentExchange)
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding notificationUpdatedBinding(Queue notificationQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(appointmentExchange)
                .with(ROUTING_KEY_UPDATED);
    }

    // ═══════════════════════════════════════
    // BINDINGS - HISTORY
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

    /**
     * RabbitTemplate com o Jackson2JsonMessageConverter explicitamente configurado.
     * Sem isto, o Spring Boot usa o SimpleMessageConverter (serialização Java binária),
     * o que impediria os consumers (notification/history) de desserializar via JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }
}
