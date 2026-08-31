package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;

public class SaveAppointmentHistoryUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public SaveAppointmentHistoryUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public AppointmentHistory execute(AppointmentHistory appointmentHistory) {
        return appointmentHistoryGateway.save(appointmentHistory);
    }
}
