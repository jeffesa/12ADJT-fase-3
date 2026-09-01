package com.fiap.notification.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de domínio Notification (POJO puro).
 * Representa uma notificação processada/enviada pelo notification-service.
 */
public class Notification {

    private UUID id;
    private String to;
    private String subject;
    private String body;
    private NotificationType type;
    private LocalDateTime sentAt;

    public Notification() {
    }

    public Notification(UUID id, String to, String subject, String body,
                        NotificationType type, LocalDateTime sentAt) {
        this.id = id;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.type = type;
        this.sentAt = sentAt;
    }

    public static Notification create(String to, String subject, String body, NotificationType type) {
        return new Notification(UUID.randomUUID(), to, subject, body, type, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
