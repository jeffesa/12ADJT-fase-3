package com.fiap.notification.infra.persistence;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para persistência de notificações.
 */
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "recipient", nullable = false)
    private String to;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public NotificationJpaEntity() {
    }

    public NotificationJpaEntity(UUID id, String to, String subject, String body,
                                 NotificationType type, LocalDateTime sentAt) {
        this.id = id;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.type = type;
        this.sentAt = sentAt;
    }

    public static NotificationJpaEntity fromDomain(Notification n) {
        return new NotificationJpaEntity(n.getId(), n.getTo(), n.getSubject(),
                n.getBody(), n.getType(), n.getSentAt());
    }

    public Notification toDomain() {
        return new Notification(id, to, subject, body, type, sentAt);
    }

    public UUID getId() {
        return id;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
