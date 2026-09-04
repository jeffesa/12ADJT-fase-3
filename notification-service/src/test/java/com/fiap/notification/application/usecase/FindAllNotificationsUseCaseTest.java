package com.fiap.notification.application.usecase;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.domain.gateway.NotificationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllNotificationsUseCaseTest {

    @Mock
    private NotificationGateway notificationGateway;

    private FindAllNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAllNotificationsUseCase(notificationGateway);
    }

    @Test
    @DisplayName("Sem tipo → retorna todas (findAll)")
    void findAllWhenTypeNull() {
        List<Notification> expected = List.of(
                Notification.create("p1", "s", "b", NotificationType.APPOINTMENT_CREATED));
        when(notificationGateway.findAll()).thenReturn(expected);

        List<Notification> result = useCase.execute(null);

        assertEquals(expected, result);
        verify(notificationGateway).findAll();
        verify(notificationGateway, never()).findByType(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Com tipo → filtra por tipo (findByType)")
    void findByTypeWhenTypeProvided() {
        List<Notification> expected = List.of(
                Notification.create("p1", "s", "b", NotificationType.APPOINTMENT_UPDATED));
        when(notificationGateway.findByType(NotificationType.APPOINTMENT_UPDATED)).thenReturn(expected);

        List<Notification> result = useCase.execute(NotificationType.APPOINTMENT_UPDATED);

        assertEquals(expected, result);
        verify(notificationGateway).findByType(NotificationType.APPOINTMENT_UPDATED);
        verify(notificationGateway, never()).findAll();
    }
}
