package com.fiap.notification.application.usecase;

import com.fiap.notification.domain.event.AppointmentEvent;

/**
 * Port de entrada: processa um evento de consulta recebido do RabbitMQ.
 * A implementação completa (montar e enviar a notificação) é da TASK-017.
 */
public interface ProcessAppointmentEventUseCase {

    void process(AppointmentEvent event);
}
