package com.fiap.notification.infra.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DlqMonitorListenerTest {

    @Mock
    private Channel channel;

    private DlqMonitorListener monitor;

    @BeforeEach
    void setUp() {
        monitor = new DlqMonitorListener();
    }

    @Test
    @DisplayName("Mensagem na DLQ → loga e dá basicAck")
    void logsAndAcks() throws IOException {
        MessageProperties props = new MessageProperties();
        props.setHeader("x-death", "rejected");
        Message message = new Message(
                "{\"appointmentId\":\"x\"}".getBytes(StandardCharsets.UTF_8), props);

        monitor.onDeadLetter(message, channel, 5L);

        verify(channel).basicAck(5L, false);
    }
}
