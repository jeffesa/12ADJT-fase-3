package com.fiap.notification.infra.messaging;

import com.fiap.notification.application.usecase.ProcessAppointmentEventUseCase;
import com.fiap.notification.domain.event.AppointmentEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumer que escuta a fila appointment.notification.queue, desserializa o
 * evento (JSON → AppointmentEvent) e delega para o use case de processamento.
 *
 * Acknowledgment MANUAL: confirma (basicAck) após processar com sucesso; em
 * falha, rejeita sem requeue (basicNack requeue=false), encaminhando a mensagem
 * para a DLQ conforme configurado na fila.
 */
@Component
@Profile("!test")
public class AppointmentNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationListener.class);

    private final ProcessAppointmentEventUseCase processAppointmentEventUseCase;
    private final RetryExecutor retryExecutor;

    public AppointmentNotificationListener(ProcessAppointmentEventUseCase processAppointmentEventUseCase,
                                           RetryExecutor retryExecutor) {
        this.processAppointmentEventUseCase = processAppointmentEventUseCase;
        this.retryExecutor = retryExecutor;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onAppointmentEvent(@Payload AppointmentEvent event,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // Processa com retry (3 tentativas, backoff exponencial). Se todas
        // falharem, rejeita sem requeue → mensagem vai para a DLQ.
        boolean processed = retryExecutor.execute(() -> processAppointmentEventUseCase.process(event));

        if (processed) {
            channel.basicAck(deliveryTag, false);
        } else {
            log.error("Evento appointmentId={} não processado após retries; enviando para a DLQ.",
                    event.appointmentId());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
