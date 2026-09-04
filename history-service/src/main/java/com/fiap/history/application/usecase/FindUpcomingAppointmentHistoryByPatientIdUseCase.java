package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.entity.UserRole;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import com.fiap.history.domain.shared.BusinessException;

import java.util.List;
import java.util.UUID;

public class FindUpcomingAppointmentHistoryByPatientIdUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindUpcomingAppointmentHistoryByPatientIdUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public List<AppointmentHistory> execute(UUID patientId, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == null) {
            throw new BusinessException("Usuário sem permissão para consultar histórico");
        }
        // Paciente só pode ver as próprias consultas futuras
        if (currentUserRole == UserRole.ROLE_PATIENT && !patientId.equals(currentUserId)) {
            throw new BusinessException("Paciente só pode visualizar o próprio histórico");
        }
        return appointmentHistoryGateway.findUpcomingByPatientId(patientId);
    }
}
