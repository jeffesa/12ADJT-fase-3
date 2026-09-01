package com.fiap.notification.domain.gateway;

import com.fiap.notification.domain.entity.Notification;

/**
 * Port de saída para envio de notificações.
 * Implementação na infra (log, e-mail, etc.) — extensível.
 */
public interface NotificationSender {

    void send(Notification notification);
}
