package com.fiap.notification.infra.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Monitor da DLQ: escuta appointment.notification.dlq e LOGA as mensagens que
 * caíram lá (após esgotarem os retries), para visibilidade/observabilidade.
 *
 * Faz ack para não reprocessar em loop; a mensagem permanece "vista" via log.
 * (A DLQ ainda tem TTL configurado; reprocessamento manual pode ser feito pela
 * RabbitMQ Management UI se necessário.)
 */
@Component
@Profile("!test")
public class DlqMonitorListener {

    private static final Logger log = LoggerFactory.getLogger(DlqMonitorListener.class);

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_DLQ, containerFactory = "dlqListenerContainerFactory")
    public void onDeadLetter(Message message,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.warn("[DLQ] Mensagem na appointment.notification.dlq: headers={} | payload={}",
                message.getMessageProperties().getHeaders(), body);
        channel.basicAck(deliveryTag, false);
    }
}
