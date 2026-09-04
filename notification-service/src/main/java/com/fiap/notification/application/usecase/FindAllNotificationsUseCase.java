package com.fiap.notification.application.usecase;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.domain.gateway.NotificationGateway;

import java.util.List;

/**
 * Lista as notificações processadas/enviadas.
 * Suporta filtro opcional por tipo.
 */
public class FindAllNotificationsUseCase {

    private final NotificationGateway notificationGateway;

    public FindAllNotificationsUseCase(NotificationGateway notificationGateway) {
        this.notificationGateway = notificationGateway;
    }

    public List<Notification> execute(NotificationType type) {
        if (type == null) {
            return notificationGateway.findAll();
        }
        return notificationGateway.findByType(type);
    }
}
