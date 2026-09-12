package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.event.AppointmentEvent;
import com.fiap.scheduling.domain.event.EventPublisher;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Envia lembretes das consultas cuja data/hora está dentro da janela configurada
 * (por padrão, próximas 24h). Publica um evento de lembrete no RabbitMQ e marca
 * a consulta como lembrada (reminderSent) para evitar duplicidade.
 *
 * POJO puro — orquestrado por um scheduler (@Scheduled) na camada de infra.
 */
public class SendAppointmentRemindersUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendAppointmentRemindersUseCase.class);

    private final AppointmentGateway appointmentGateway;
    private final EventPublisher eventPublisher;
    private final long windowHours;

    public SendAppointmentRemindersUseCase(AppointmentGateway appointmentGateway,
                                           EventPublisher eventPublisher,
                                           long windowHours) {
        this.appointmentGateway = appointmentGateway;
        this.eventPublisher = eventPublisher;
        this.windowHours = windowHours;
    }

    /**
     * Busca consultas elegíveis (SCHEDULED/CONFIRMED, dateTime entre agora e agora+janela,
     * sem lembrete enviado), publica o evento de lembrete e marca cada uma como lembrada.
     *
     * @return quantidade de lembretes enviados
     */
    public int execute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(windowHours);

        List<Appointment> remindable = appointmentGateway.findRemindableWithin(now, limit);
        if (remindable.isEmpty()) {
            return 0;
        }

        int sent = 0;
        for (Appointment appointment : remindable) {
            try {
                eventPublisher.publish(AppointmentEvent.reminder(appointment));
                appointment.markReminderSent();
                appointmentGateway.update(appointment);
                sent++;
            } catch (Exception e) {
                // Não interrompe o lote: um lembrete que falha não bloqueia os demais.
                // Como reminderSent não foi marcado, será reprocessado na próxima execução.
                log.error("Falha ao enviar lembrete da consulta {}: {}", appointment.getId(), e.getMessage());
            }
        }

        log.info("Lembretes enviados: {}/{} (janela de {}h)", sent, remindable.size(), windowHours);
        return sent;
    }
}
