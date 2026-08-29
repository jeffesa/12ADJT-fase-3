package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;

import java.util.UUID;

public class FindAppointmentByIdUseCase {

    private final AppointmentGateway appointmentGateway;

    public FindAppointmentByIdUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public Appointment execute(UUID appointmentId, UUID currentUserId, UserRole currentUserRole) {
        Appointment appointment = appointmentGateway.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada"));

        if (currentUserRole == UserRole.ROLE_PATIENT && !appointment.getPatientId().equals(currentUserId)) {
            throw new BusinessException("Paciente só pode visualizar suas próprias consultas");
        }

        return appointment;
    }
}
