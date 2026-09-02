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
import static org.mockito.Mockito.times;
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
        // RetryExecutor real, 3 tentativas, intervalos curtos (1ms) para teste rápido
        RetryExecutor retry = new RetryExecutor(3, 1L, 2.0);
        listener = new AppointmentNotificationListener(useCase, retry);
    }

    private AppointmentEvent event() {
        return new AppointmentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.now().plusDays(1), "SCHEDULED", AppointmentEventType.CREATED);
    }

    @Test
    @DisplayName("Sucesso: processa uma vez e faz basicAck")
    void successAcks() throws Exception {
        AppointmentEvent ev = event();

        listener.onAppointmentEvent(ev, channel, 42L);

        verify(useCase, times(1)).process(ev);
        verify(channel).basicAck(eq(42L), eq(false));
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("Falha em todas as tentativas: 3 tentativas e basicNack sem requeue (DLQ)")
    void failureRetriesThenNacksToDlq() throws Exception {
        AppointmentEvent ev = event();
        doThrow(new RuntimeException("erro")).when(useCase).process(ev);

        listener.onAppointmentEvent(ev, channel, 7L);

        // 3 tentativas antes de desistir
        verify(useCase, times(3)).process(ev);
        verify(channel).basicNack(eq(7L), eq(false), eq(false));
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
