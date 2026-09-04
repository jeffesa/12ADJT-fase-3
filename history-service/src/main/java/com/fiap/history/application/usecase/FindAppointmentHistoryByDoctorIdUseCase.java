package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.entity.UserRole;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import com.fiap.history.domain.shared.BusinessException;

import java.util.List;
import java.util.UUID;

public class FindAppointmentHistoryByDoctorIdUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindAppointmentHistoryByDoctorIdUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public List<AppointmentHistory> execute(UUID doctorId, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == null) {
            throw new BusinessException("Usuário sem permissão para consultar histórico");
        }
        // Paciente não pode consultar históricos por médico
        if (currentUserRole == UserRole.ROLE_PATIENT) {
            throw new BusinessException("Paciente não pode consultar histórico por médico");
        }
        // Médico só pode ver o próprio histórico; enfermeiro (NURSE) tem acesso amplo
        if (currentUserRole == UserRole.ROLE_DOCTOR && !doctorId.equals(currentUserId)) {
            throw new BusinessException("Médico só pode visualizar o próprio histórico");
        }
        return appointmentHistoryGateway.findByDoctorId(doctorId);
    }
}
