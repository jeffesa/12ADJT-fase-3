package com.fiap.notification.application.usecase;

import com.fiap.notification.domain.entity.Notification;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.domain.event.AppointmentEvent;
import com.fiap.notification.domain.event.AppointmentEventType;
import com.fiap.notification.domain.gateway.NotificationGateway;
import com.fiap.notification.domain.gateway.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @Mock
    private NotificationSender sender;
    @Mock
    private NotificationGateway gateway;

    private SendNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendNotificationUseCase(sender, gateway);
    }

    private AppointmentEvent event(AppointmentEventType type) {
        return new AppointmentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2099, 12, 1, 14, 30), "SCHEDULED", "Consulta", type);
    }

    @Test
    @DisplayName("CREATED: envia e persiste notificação do tipo APPOINTMENT_CREATED")
    void createdNotification() {
        when(gateway.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

        useCase.process(event(AppointmentEventType.CREATED));

        ArgumentCaptor<Notification> sent = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(sent.capture());
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(gateway).save(saved.capture());

        assertThat(sent.getValue().getType()).isEqualTo(NotificationType.APPOINTMENT_CREATED);
        assertThat(sent.getValue().getSubject()).isEqualTo("Consulta agendada");
        assertThat(sent.getValue().getSentAt()).isNotNull();
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.APPOINTMENT_CREATED);
    }

    @Test
    @DisplayName("UPDATED: envia e persiste notificação do tipo APPOINTMENT_UPDATED")
    void updatedNotification() {
        when(gateway.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));

        useCase.process(event(AppointmentEventType.UPDATED));

        ArgumentCaptor<Notification> sent = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(sent.capture());
        verify(gateway).save(org.mockito.ArgumentMatchers.any());

        assertThat(sent.getValue().getType()).isEqualTo(NotificationType.APPOINTMENT_UPDATED);
        assertThat(sent.getValue().getSubject()).isEqualTo("Consulta atualizada");
    }

    @Test
    @DisplayName("Gatilho FORCE_ERROR: lança exceção e NÃO envia nem persiste (dispara DLQ)")
    void forceErrorThrows() {
        AppointmentEvent ev = new AppointmentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.of(2099, 12, 1, 14, 30), "SCHEDULED",
                SendNotificationUseCase.FORCE_ERROR_TRIGGER, AppointmentEventType.CREATED);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> useCase.process(ev));

        org.mockito.Mockito.verifyNoInteractions(sender);
        org.mockito.Mockito.verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("Envia ANTES de persistir e o corpo contém dados do evento")
    void bodyContainsEventData() {
        when(gateway.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
        AppointmentEvent ev = event(AppointmentEventType.CREATED);

        useCase.process(ev);

        ArgumentCaptor<Notification> sent = ArgumentCaptor.forClass(Notification.class);
        verify(sender).send(sent.capture());
        assertThat(sent.getValue().getBody()).contains(ev.appointmentId().toString());
        assertThat(sent.getValue().getTo()).contains(ev.patientId().toString());
    }
}
