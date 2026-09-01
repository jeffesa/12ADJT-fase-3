package com.fiap.notification.infra.persistence;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração da persistência (JPA + H2, profile test).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(NotificationJpaGateway.class)
class NotificationJpaGatewayTest {

    @Autowired
    private NotificationJpaGateway gateway;

    @Test
    @DisplayName("save persiste e findAll retorna a notificação")
    void saveAndFindAll() {
        Notification n = Notification.create("patient:123", "Assunto", "Corpo", NotificationType.APPOINTMENT_CREATED);

        gateway.save(n);

        List<Notification> all = gateway.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getSubject()).isEqualTo("Assunto");
        assertThat(all.get(0).getType()).isEqualTo(NotificationType.APPOINTMENT_CREATED);
    }

    @Test
    @DisplayName("findByType filtra corretamente")
    void findByType() {
        gateway.save(Notification.create("a", "s1", "b1", NotificationType.APPOINTMENT_CREATED));
        gateway.save(Notification.create("b", "s2", "b2", NotificationType.APPOINTMENT_UPDATED));
        gateway.save(Notification.create("c", "s3", "b3", NotificationType.APPOINTMENT_CREATED));

        assertThat(gateway.findByType(NotificationType.APPOINTMENT_CREATED)).hasSize(2);
        assertThat(gateway.findByType(NotificationType.APPOINTMENT_UPDATED)).hasSize(1);
    }
}
