package com.fiap.notification.application.usecase;

import com.fiap.notification.domain.event.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementação provisória do processamento de evento (TASK-016): apenas loga.
 * Será substituída/expandida na TASK-017 (SendNotificationUseCase), que monta
 * e envia a notificação via NotificationSender.
 */
@Component
public class LogProcessAppointmentEventUseCase implements ProcessAppointmentEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogProcessAppointmentEventUseCase.class);

    @Override
    public void process(AppointmentEvent event) {
        log.info("Evento de consulta recebido: type={}, appointmentId={}, patientId={}, doctorId={}, dateTime={}, status={}",
                event.eventType(), event.appointmentId(), event.patientId(),
                event.doctorId(), event.dateTime(), event.status());
    }
}
