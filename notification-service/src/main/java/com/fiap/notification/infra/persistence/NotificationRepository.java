package com.fiap.notification.infra.persistence;

import com.fiap.notification.domain.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository para NotificationJpaEntity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByType(NotificationType type);
}
