package com.fiap.notification.application.usecase;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.domain.event.AppointmentEvent;
import com.fiap.notification.domain.event.AppointmentEventType;
import com.fiap.notification.domain.gateway.NotificationGateway;
import com.fiap.notification.domain.gateway.NotificationSender;

/**
 * Caso de uso: processa um evento de consulta, monta a notificação,
 * envia via NotificationSender e persiste o registro via NotificationGateway.
 *
 * Implementa o port ProcessAppointmentEventUseCase, consumido pelo listener.
 */
public class SendNotificationUseCase implements ProcessAppointmentEventUseCase {

    private final NotificationSender notificationSender;
    private final NotificationGateway notificationGateway;

    public SendNotificationUseCase(NotificationSender notificationSender,
                                   NotificationGateway notificationGateway) {
        this.notificationSender = notificationSender;
        this.notificationGateway = notificationGateway;
    }

    /**
     * Gatilho para DEMONSTRAÇÃO/TESTE da DLQ: quando a descrição da consulta é
     * exatamente "FORCE_ERROR", o processamento falha de propósito. Isso permite
     * simular uma falha real de processamento (via API REST do scheduling),
     * exercitando o retry (3 tentativas) e o envio para a DLQ.
     */
    public static final String FORCE_ERROR_TRIGGER = "FORCE_ERROR";

    @Override
    public void process(AppointmentEvent event) {
        if (FORCE_ERROR_TRIGGER.equals(event.description())) {
            throw new IllegalStateException(
                    "Falha simulada (FORCE_ERROR) para teste de DLQ - appointmentId=" + event.appointmentId());
        }

        NotificationType type = mapType(event.eventType());
        String subject = buildSubject(type);
        String body = buildBody(event, type);
        // Destinatário: na ausência de contato do paciente no evento, usa o patientId como referência.
        String to = "patient:" + event.patientId();

        Notification notification = Notification.create(to, subject, body, type);

        notificationSender.send(notification);
        notificationGateway.save(notification);
    }

    private NotificationType mapType(AppointmentEventType eventType) {
        return switch (eventType) {
            case CREATED -> NotificationType.APPOINTMENT_CREATED;
            case UPDATED -> NotificationType.APPOINTMENT_UPDATED;
        };
    }

    private String buildSubject(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_CREATED -> "Consulta agendada";
            case APPOINTMENT_UPDATED -> "Consulta atualizada";
            case APPOINTMENT_REMINDER -> "Lembrete de consulta";
        };
    }

    private String buildBody(AppointmentEvent event, NotificationType type) {
        String acao = switch (type) {
            case APPOINTMENT_CREATED -> "agendada";
            case APPOINTMENT_UPDATED -> "atualizada";
            case APPOINTMENT_REMINDER -> "lembrada";
        };
        return String.format(
                "Sua consulta (id %s) com o médico %s foi %s para %s. Status atual: %s.",
                event.appointmentId(), event.doctorId(), acao, event.dateTime(), event.status());
    }
}
