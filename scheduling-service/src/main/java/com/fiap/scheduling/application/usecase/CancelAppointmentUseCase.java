package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.AccessDeniedException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;

import java.util.UUID;

public class CancelAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;

    public CancelAppointmentUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public Appointment execute(UUID appointmentId, UUID currentUserId, UserRole currentUserRole) {
        Appointment appointment = appointmentGateway.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada"));

        boolean canCancel = currentUserRole == UserRole.ROLE_DOCTOR
                || currentUserRole == UserRole.ROLE_NURSE
                || appointment.getPatientId().equals(currentUserId);

        if (!canCancel) {
            throw new AccessDeniedException("Usuário sem permissão para cancelar esta consulta");
        }

        appointment.cancel();
        return appointmentGateway.update(appointment);
    }
}
