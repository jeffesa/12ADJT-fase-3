package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.entity.UserRole;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import com.fiap.history.domain.shared.BusinessException;

import java.util.List;

public class FindAllAppointmentHistoryUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindAllAppointmentHistoryUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public List<AppointmentHistory> execute(UserRole currentUserRole) {
        // Listagem completa é administrativa: paciente não pode acessar
        if (currentUserRole == null || currentUserRole == UserRole.ROLE_PATIENT) {
            throw new BusinessException("Sem permissão para listar todo o histórico");
        }
        return appointmentHistoryGateway.findAll();
    }
}
