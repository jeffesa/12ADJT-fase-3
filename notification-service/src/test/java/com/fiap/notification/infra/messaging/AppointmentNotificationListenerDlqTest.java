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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Testa o comportamento de ack/nack do listener — a lógica que decide se a
 * mensagem é confirmada (sucesso) ou rejeitada para a DLQ (falha após retries).
 * Usa RetryExecutor com 1 tentativa para tornar o teste rápido e determinístico.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentNotificationListenerDlqTest {

    @Mock
    private ProcessAppointmentEventUseCase useCase;

    @Mock
    private Channel channel;

    private AppointmentNotificationListener listener;

    @BeforeEach
    void setUp() {
        // maxAttempts=1 → sem espera; falha vai direto para nack (DLQ)
        RetryExecutor retryExecutor = new RetryExecutor(1, 1, 2.0);
        listener = new AppointmentNotificationListener(useCase, retryExecutor);
    }

    private AppointmentEvent event() {
        return new AppointmentEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.now().plusDays(1), "SCHEDULED", "Consulta", AppointmentEventType.CREATED);
    }

    @Test
    @DisplayName("Sucesso → basicAck (confirma a mensagem)")
    void successAcks() throws IOException {
        AppointmentEvent ev = event();

        listener.onAppointmentEvent(ev, channel, 10L);

        verify(useCase).process(ev);
        verify(channel).basicAck(10L, false);
    }

    @Test
    @DisplayName("Falha após retries → basicNack sem requeue (vai para a DLQ)")
    void failureNacksToDlq() throws IOException {
        AppointmentEvent ev = event();
        doThrow(new RuntimeException("falha")).when(useCase).process(ev);

        listener.onAppointmentEvent(ev, channel, 20L);

        // nack com requeue=false → mensagem encaminhada para a DLQ
        verify(channel).basicNack(20L, false, false);
    }
}
