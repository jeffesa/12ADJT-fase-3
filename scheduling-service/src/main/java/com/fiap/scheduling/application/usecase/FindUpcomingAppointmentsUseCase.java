package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.shared.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FindUpcomingAppointmentsUseCase {

    private final AppointmentGateway appointmentGateway;

    public FindUpcomingAppointmentsUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public List<Appointment> execute(UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        List<Appointment> upcomingAppointments = appointmentGateway.findUpcoming(LocalDateTime.now());

        return switch (currentUserRole) {
            case ROLE_PATIENT -> upcomingAppointments.stream()
                    .filter(appointment -> appointment.getPatientId().equals(currentUserId))
                    .toList();
            case ROLE_DOCTOR -> upcomingAppointments.stream()
                    .filter(appointment -> appointment.getDoctorId().equals(currentUserId))
                    .toList();
            case ROLE_NURSE -> upcomingAppointments;
        };
    }
}
