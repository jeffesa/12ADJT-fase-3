package com.fiap.notification.infra.persistence;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.domain.gateway.NotificationGateway;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementação do NotificationGateway usando JPA.
 */
@Component
public class NotificationJpaGateway implements NotificationGateway {

    private final NotificationRepository repository;

    public NotificationJpaGateway(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity saved = repository.save(NotificationJpaEntity.fromDomain(notification));
        return saved.toDomain();
    }

    @Override
    public List<Notification> findAll() {
        return repository.findAll().stream().map(NotificationJpaEntity::toDomain).toList();
    }

    @Override
    public List<Notification> findByType(NotificationType type) {
        return repository.findByType(type).stream().map(NotificationJpaEntity::toDomain).toList();
    }
}
