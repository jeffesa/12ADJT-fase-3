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

public class CreateAppointmentUseCase {

    private final AppointmentGateway appointmentGateway;
    private final UserGateway userGateway;
    private final EventPublisher eventPublisher;

    public CreateAppointmentUseCase(AppointmentGateway appointmentGateway,
                                    UserGateway userGateway,
                                    EventPublisher eventPublisher) {
        this.appointmentGateway = appointmentGateway;
        this.userGateway = userGateway;
        this.eventPublisher = eventPublisher;
    }

    public Appointment execute(UUID patientId, UUID doctorId, LocalDateTime dateTime,
                               String description, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole != UserRole.ROLE_DOCTOR && currentUserRole != UserRole.ROLE_NURSE) {
            throw new BusinessException("Apenas médicos e enfermeiros podem criar consultas");
        }

        if (currentUserRole == UserRole.ROLE_DOCTOR && !doctorId.equals(currentUserId)) {
            throw new BusinessException("Médico só pode agendar consultas para si");
        }

        User patient = userGateway.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
        if (patient.getRole() != UserRole.ROLE_PATIENT) {
            throw new BusinessException("O usuário informado não é um paciente");
        }

        User doctor = userGateway.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Médico não encontrado"));
        if (doctor.getRole() != UserRole.ROLE_DOCTOR) {
            throw new BusinessException("O usuário informado não é um médico");
        }

        Appointment appointment = Appointment.create(patientId, doctorId, dateTime, description);
        Appointment saved = appointmentGateway.create(appointment);
        eventPublisher.publish(AppointmentEvent.created(saved));
        return saved;
    }
}
