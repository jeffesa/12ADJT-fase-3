package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;

import java.util.List;

/**
 * Caso de uso: listar todas as consultas.
 * Se um status for informado, filtra por ele; senão, retorna todas.
 * O controle de acesso (apenas DOCTOR/NURSE) é feito no SecurityConfig.
 */
public class FindAllAppointmentsUseCase {

    private final AppointmentGateway appointmentGateway;

    public FindAllAppointmentsUseCase(AppointmentGateway appointmentGateway) {
        this.appointmentGateway = appointmentGateway;
    }

    public List<Appointment> execute(AppointmentStatus statusFilter) {
        List<Appointment> appointments = appointmentGateway.findAll();

        if (statusFilter == null) {
            return appointments;
        }

        return appointments.stream()
                .filter(appointment -> appointment.getStatus() == statusFilter)
                .toList();
    }
}
