package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;

import java.util.UUID;

public class FindAppointmentHistoryByIdUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindAppointmentHistoryByIdUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public AppointmentHistory execute(UUID id) {
        return appointmentHistoryGateway.findById(id);
    }
}
