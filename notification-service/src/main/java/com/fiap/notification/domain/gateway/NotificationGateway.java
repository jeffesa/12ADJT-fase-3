package com.fiap.notification.domain.gateway;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;

import java.util.List;

/**
 * Port de saída para persistência de notificações.
 * Implementação JPA na infra.
 */
public interface NotificationGateway {

    Notification save(Notification notification);

    List<Notification> findAll();

    List<Notification> findByType(NotificationType type);
}
