package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.event.AppointmentEvent;
import com.fiap.scheduling.domain.event.EventPublisher;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.AccessDeniedException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;

import java.util.UUID;

/**
 * Marca uma consulta como realizada (CONFIRMED -> COMPLETED). Apenas DOCTOR e NURSE.
 * Publica evento UPDATED para alimentar notificação e histórico.
 */
public class CompleteAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final EventPublisher eventPublisher;

    public CompleteAppointmentUseCase(AppointmentGateway appointmentGateway, EventPublisher eventPublisher) {
        this.appointmentGateway = appointmentGateway;
        this.eventPublisher = eventPublisher;
    }

    public Appointment execute(UUID appointmentId, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole != UserRole.ROLE_DOCTOR && currentUserRole != UserRole.ROLE_NURSE) {
            throw new AccessDeniedException("Apenas médicos e enfermeiros podem concluir consultas");
        }

        Appointment appointment = appointmentGateway.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada"));

        if (currentUserRole == UserRole.ROLE_DOCTOR && !appointment.getDoctorId().equals(currentUserId)) {
            throw new AccessDeniedException("Médico só pode concluir suas consultas");
        }

        appointment.complete();
        Appointment updated = appointmentGateway.update(appointment);
        eventPublisher.publish(AppointmentEvent.updated(updated));
        return updated;
    }
}
