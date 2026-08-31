package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;

import java.util.List;

public class FindAllAppointmentHistoryUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindAllAppointmentHistoryUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public List<AppointmentHistory> execute() {
        return appointmentHistoryGateway.findAll();
    }
}
