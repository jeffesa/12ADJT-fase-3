package com.fiap.notification.infra.messaging;

import org.springframework.amqp.core.AcknowledgeMode;
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

/**
 * Configuração do RabbitMQ para o notification-service (consumer).
 * Define exchange, queues, bindings, DLQ e política de retry.
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

    // Routing Keys
    public static final String ROUTING_KEY_CREATED = "appointment.created";
    public static final String ROUTING_KEY_UPDATED = "appointment.updated";

    // Concurrency
    private static final int CONCURRENT_CONSUMERS = 2;
    private static final int MAX_CONCURRENT_CONSUMERS = 5;
    private static final int PREFETCH_COUNT = 10;

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
    // BINDINGS
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
    // MESSAGE CONVERTER (JSON)
    // ═══════════════════════════════════════

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ═══════════════════════════════════════
    // LISTENER CONTAINER (concurrency + ack manual)
    // ═══════════════════════════════════════

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jackson2JsonMessageConverter,
            @org.springframework.beans.factory.annotation.Value(
                    "${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);

        // Concurrency: consumers simultâneos
        factory.setConcurrentConsumers(CONCURRENT_CONSUMERS);
        factory.setMaxConcurrentConsumers(MAX_CONCURRENT_CONSUMERS);
        factory.setPrefetchCount(PREFETCH_COUNT);

        // Acknowledgment MANUAL: o listener faz basicAck/basicNack explicitamente.
        // (Política de retry com backoff será tratada na TASK-018.)
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // Respeita a config de auto-startup (permite desligar em testes de wiring)
        factory.setAutoStartup(autoStartup);

        return factory;
    }
}
