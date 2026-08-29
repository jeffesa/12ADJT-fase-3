package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.BusinessException;

import java.util.List;
import java.util.UUID;

public class FindAppointmentsByPatientUseCase {

    private final AppointmentGateway appointmentGateway;

    public FindAppointmentsByPatientUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public List<Appointment> execute(UUID patientId, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == UserRole.ROLE_PATIENT && !patientId.equals(currentUserId)) {
            throw new BusinessException("Paciente só pode visualizar suas próprias consultas");
        }

        if (currentUserRole != UserRole.ROLE_PATIENT
                && currentUserRole != UserRole.ROLE_DOCTOR
                && currentUserRole != UserRole.ROLE_NURSE) {
            throw new BusinessException("Usuário sem permissão para consultar consultas de paciente");
        }

        return appointmentGateway.findByPatientId(patientId);
    }
}
