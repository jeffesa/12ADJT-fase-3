package com.fiap.notification.infra.web.dto;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta da listagem de notificações.
 */
public record NotificationResponse(
        UUID id,
        String to,
        String subject,
        String body,
        NotificationType type,
        LocalDateTime sentAt
) {
    public static NotificationResponse fromDomain(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTo(),
                notification.getSubject(),
                notification.getBody(),
                notification.getType(),
                notification.getSentAt()
        );
    }
}
