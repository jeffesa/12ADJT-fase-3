package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.BusinessException;

import java.util.List;
import java.util.UUID;

public class FindAppointmentsByDoctorUseCase {

    private final AppointmentGateway appointmentGateway;

    public FindAppointmentsByDoctorUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public List<Appointment> execute(UUID doctorId, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == UserRole.ROLE_DOCTOR && !doctorId.equals(currentUserId)) {
            throw new BusinessException("Médico só pode visualizar suas próprias consultas");
        }

        if (currentUserRole != UserRole.ROLE_DOCTOR && currentUserRole != UserRole.ROLE_NURSE) {
            throw new BusinessException("Usuário sem permissão para consultar consultas de médico");
        }

        return appointmentGateway.findByDoctorId(doctorId);
    }
}
