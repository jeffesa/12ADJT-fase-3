package com.fiap.notification.infra.messaging;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class LogNotificationSenderTest {

    private final LogNotificationSender sender = new LogNotificationSender();

    @Test
    @DisplayName("send não lança e processa a notificação (simula envio via log)")
    void sendDoesNotThrow() {
        Notification n = Notification.create(
                "patient:123", "Consulta agendada", "corpo", NotificationType.APPOINTMENT_CREATED);

        assertThatCode(() -> sender.send(n)).doesNotThrowAnyException();
    }
}
