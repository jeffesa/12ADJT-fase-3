package com.fiap.scheduling.domain.event;

public interface EventPublisher {
    void publish(AppointmentEvent event);
}
