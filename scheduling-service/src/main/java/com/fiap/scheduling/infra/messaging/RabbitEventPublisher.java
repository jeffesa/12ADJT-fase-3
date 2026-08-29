package com.fiap.scheduling.infra.messaging;

import com.fiap.scheduling.domain.event.AppointmentEvent;
import com.fiap.scheduling.domain.event.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(AppointmentEvent event) {
        String routingKey = switch (event.eventType()) {
            case CREATED -> RabbitConfig.ROUTING_KEY_CREATED;
            case UPDATED -> RabbitConfig.ROUTING_KEY_UPDATED;
        };

        rabbitTemplate.convertAndSend(RabbitConfig.APPOINTMENT_EXCHANGE, routingKey, event);
    }
}
