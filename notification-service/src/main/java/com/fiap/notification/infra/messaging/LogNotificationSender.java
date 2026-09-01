package com.fiap.notification.infra.messaging;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.gateway.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação de NotificationSender que simula o envio logando no console.
 * Estrutura extensível — pode ser substituída/complementada por um
 * EmailNotificationSender (JavaMailSender/MailHog) no futuro.
 */
@Component
public class LogNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationSender.class);

    @Override
    public void send(Notification notification) {
        log.info("[NOTIFICAÇÃO ENVIADA] type={} | to={} | subject='{}' | body='{}'",
                notification.getType(), notification.getTo(),
                notification.getSubject(), notification.getBody());
    }
}
