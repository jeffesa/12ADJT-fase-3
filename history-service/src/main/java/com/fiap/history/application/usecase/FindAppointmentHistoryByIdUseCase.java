package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.entity.UserRole;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import com.fiap.history.domain.shared.BusinessException;

import java.util.UUID;

public class FindAppointmentHistoryByIdUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindAppointmentHistoryByIdUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public AppointmentHistory execute(UUID id, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == null) {
            throw new BusinessException("Usuário sem permissão para consultar histórico");
        }
        AppointmentHistory history = appointmentHistoryGateway.findById(id);

        // Paciente só pode ver registros dos quais é o paciente
        if (currentUserRole == UserRole.ROLE_PATIENT && !currentUserId.equals(history.getPatientId())) {
            throw new BusinessException("Paciente só pode visualizar o próprio histórico");
        }
        // Médico só pode ver registros dos quais é o médico
        if (currentUserRole == UserRole.ROLE_DOCTOR && !currentUserId.equals(history.getDoctorId())) {
            throw new BusinessException("Médico só pode visualizar o próprio histórico");
        }
        return history;
    }
}
