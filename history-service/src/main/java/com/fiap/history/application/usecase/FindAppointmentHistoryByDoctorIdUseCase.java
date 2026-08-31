package com.fiap.history.application.usecase;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;

import java.util.List;
import java.util.UUID;

public class FindAppointmentHistoryByDoctorIdUseCase {

    private final AppointmentHistoryGateway appointmentHistoryGateway;

    public FindAppointmentHistoryByDoctorIdUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        this.appointmentHistoryGateway = appointmentHistoryGateway;
    }

    public List<AppointmentHistory> execute(UUID doctorId) {
        return appointmentHistoryGateway.findByDoctorId(doctorId);
    }
}
