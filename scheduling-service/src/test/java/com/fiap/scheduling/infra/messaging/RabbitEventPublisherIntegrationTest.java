package com.fiap.scheduling.infra.messaging;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
import com.fiap.scheduling.domain.event.AppointmentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Prova (sem broker externo) do comportamento de publicação da TASK-014:
 * 1. o publisher envia para a exchange e routing key corretas (created/updated)
 * 2. o AppointmentEvent é serializável em JSON pelo Jackson2JsonMessageConverter
 *    (content-type application/json + campos esperados no corpo)
 */
class RabbitEventPublisherIntegrationTest {

    private Appointment sampleAppointment(AppointmentStatus status) {
        return new Appointment(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 12, 1, 14, 30),
                status, "Consulta",
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void shouldSendToCorrectExchangeAndRoutingKeyForCreated() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitEventPublisher publisher = new RabbitEventPublisher(rabbitTemplate);

        Appointment appointment = sampleAppointment(AppointmentStatus.SCHEDULED);
        AppointmentEvent event = AppointmentEvent.created(appointment);

        publisher.publish(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                eq("appointment.exchange"),
                eq("appointment.created"),
                payloadCaptor.capture());

        assertThat(payloadCaptor.getValue()).isSameAs(event);
    }

    @Test
    void shouldSendToUpdatedRoutingKeyForUpdated() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitEventPublisher publisher = new RabbitEventPublisher(rabbitTemplate);

        publisher.publish(AppointmentEvent.updated(sampleAppointment(AppointmentStatus.CONFIRMED)));

        verify(rabbitTemplate).convertAndSend(
                eq("appointment.exchange"),
                eq("appointment.updated"),
                any(Object.class));
    }

    @Test
    void appointmentEventShouldSerializeToJson() {
        // Usa o MESMO converter declarado na RabbitConfig
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment(
                appointmentId, UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2026, 12, 1, 14, 30),
                AppointmentStatus.SCHEDULED, "Consulta",
                LocalDateTime.now(), LocalDateTime.now()
        );
        AppointmentEvent event = AppointmentEvent.created(appointment);

        Message message = converter.toMessage(event, new MessageProperties());
        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        assertThat(message.getMessageProperties().getContentType())
                .as("content-type deve ser application/json")
                .contains("application/json");

        assertThat(json)
                .contains("appointmentId")
                .contains("patientId")
                .contains("doctorId")
                .contains("dateTime")
                .contains("status")
                .contains("eventType")
                .contains("CREATED")
                .contains(appointmentId.toString());
    }
}
