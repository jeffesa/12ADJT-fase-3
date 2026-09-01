package com.fiap.notification.infra.messaging;

import com.fiap.notification.application.usecase.ProcessAppointmentEventUseCase;
import com.fiap.notification.domain.event.AppointmentEvent;
import com.fiap.notification.domain.event.AppointmentEventType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentNotificationListenerTest {

    @Mock
    private ProcessAppointmentEventUseCase useCase;
    @Mock
    private Channel channel;

    private AppointmentNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new AppointmentNotificationListener(useCase);
    }

    private AppointmentEvent event() {
        return new AppointmentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.now().plusDays(1), "SCHEDULED", AppointmentEventType.CREATED);
    }

    @Test
    @DisplayName("Sucesso: delega ao use case e faz basicAck")
    void successAcks() throws Exception {
        AppointmentEvent ev = event();
        long deliveryTag = 42L;

        listener.onAppointmentEvent(ev, channel, deliveryTag);

        verify(useCase).process(ev);
        verify(channel).basicAck(eq(deliveryTag), eq(false));
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("Falha no processamento: basicNack sem requeue (vai para DLQ)")
    void failureNacksToDlq() throws Exception {
        AppointmentEvent ev = event();
        long deliveryTag = 7L;
        doThrow(new RuntimeException("erro")).when(useCase).process(ev);

        listener.onAppointmentEvent(ev, channel, deliveryTag);

        verify(channel).basicNack(eq(deliveryTag), eq(false), eq(false));
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
