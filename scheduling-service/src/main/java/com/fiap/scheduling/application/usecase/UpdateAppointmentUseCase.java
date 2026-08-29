package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.event.AppointmentEvent;
import com.fiap.scheduling.domain.event.EventPublisher;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

public class UpdateAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final UserGateway userGateway;
    private final EventPublisher eventPublisher;

    public UpdateAppointmentUseCase(AppointmentGateway appointmentGateway,
                                    UserGateway userGateway,
                                    EventPublisher eventPublisher) {
        this.appointmentGateway = appointmentGateway;
        this.userGateway = userGateway;
        this.eventPublisher = eventPublisher;
    }

    public Appointment execute(UUID appointmentId, UUID patientId, UUID doctorId,
                               LocalDateTime dateTime, String description,
                               UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole != UserRole.ROLE_DOCTOR && currentUserRole != UserRole.ROLE_NURSE) {
            throw new BusinessException("Apenas médicos e enfermeiros podem editar consultas");
        }

        Appointment appointment = appointmentGateway.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada"));

        if (currentUserRole == UserRole.ROLE_DOCTOR && !appointment.getDoctorId().equals(currentUserId)) {
            throw new BusinessException("Médico só pode editar suas consultas");
        }

        if (patientId != null) {
            User patient = userGateway.findById(patientId)
                    .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
            if (patient.getRole() != UserRole.ROLE_PATIENT) {
                throw new BusinessException("O usuário informado não é um paciente");
            }
            appointment.setPatientId(patientId);
        }

        if (doctorId != null) {
            User doctor = userGateway.findById(doctorId)
                    .orElseThrow(() -> new EntityNotFoundException("Médico não encontrado"));
            if (doctor.getRole() != UserRole.ROLE_DOCTOR) {
                throw new BusinessException("O usuário informado não é um médico");
            }
            appointment.setDoctorId(doctorId);
        }

        appointment.update(dateTime, description);
        Appointment updated = appointmentGateway.update(appointment);
        eventPublisher.publish(AppointmentEvent.updated(updated));
        return updated;
    }
}
