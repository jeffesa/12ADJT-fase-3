package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;

import java.util.List;
import java.util.UUID;

public class FindUpcomingAppointmentHistoryByPatientIdUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindUpcomingAppointmentHistoryByPatientIdUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public List<AppointmentHistory> execute(UUID patientId) {
        return appointmentHistoryGateway.findUpcomingByPatientId(patientId);
    }
}
