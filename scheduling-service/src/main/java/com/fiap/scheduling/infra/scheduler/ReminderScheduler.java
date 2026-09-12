package com.fiap.scheduling.infra.scheduler;

import com.fiap.scheduling.application.usecase.SendAppointmentRemindersUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periodicamente o envio de lembretes de consultas próximas.
 * Bean fino: apenas delega ao use case (a lógica testável fica no use case).
 * Desativado no profile 'test' para não agendar durante os testes.
 */
@Component
@Profile("!test")
public class ReminderScheduler {

    private final SendAppointmentRemindersUseCase sendAppointmentRemindersUseCase;

    public ReminderScheduler(SendAppointmentRemindersUseCase sendAppointmentRemindersUseCase) {
        this.sendAppointmentRemindersUseCase = sendAppointmentRemindersUseCase;
    }

    /**
     * Executa em intervalo fixo (padrão: a cada 15 min), configurável via
     * app.reminder.fixed-rate-ms. O primeiro disparo aguarda app.reminder.initial-delay-ms.
     */
    @Scheduled(
            fixedRateString = "${app.reminder.fixed-rate-ms:900000}",
            initialDelayString = "${app.reminder.initial-delay-ms:60000}")
    public void sendReminders() {
        sendAppointmentRemindersUseCase.execute();
    }
}
