package com.fiap.history.infra.messaging;

import com.fiap.history.application.usecase.SaveAppointmentHistoryUseCase;
import com.fiap.history.domain.entity.AppointmentHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AppointmentHistoryListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentHistoryListener.class);

    private final SaveAppointmentHistoryUseCase saveAppointmentHistoryUseCase;

    public AppointmentHistoryListener(SaveAppointmentHistoryUseCase saveAppointmentHistoryUseCase) {
        this.saveAppointmentHistoryUseCase = saveAppointmentHistoryUseCase;
    }

    @RabbitListener(queues = RabbitConfig.HISTORY_QUEUE)
    public void listen(AppointmentHistoryEvent event) {
        if (event == null) {
            log.warn("Evento de histórico recebido nulo");
            return;
        }

        AppointmentHistory appointmentHistory = new AppointmentHistory();
        appointmentHistory.setId(UUID.randomUUID());
        appointmentHistory.setAppointmentId(event.appointmentId());
        appointmentHistory.setPatientId(event.patientId());
        appointmentHistory.setDoctorId(event.doctorId());
        appointmentHistory.setDateTime(event.dateTime());
        appointmentHistory.setStatus(event.status());
        appointmentHistory.setDescription(null);
        appointmentHistory.setEventType(event.eventType());
        appointmentHistory.setReceivedAt(LocalDateTime.now());

        saveAppointmentHistoryUseCase.execute(appointmentHistory);
        log.info("Histórico salvo para appointmentId {} e eventType {}", event.appointmentId(), event.eventType());
    }
}
