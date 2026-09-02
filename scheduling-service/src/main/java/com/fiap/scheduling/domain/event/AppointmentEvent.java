package com.fiap.scheduling.domain.event;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId,
        LocalDateTime dateTime,
        AppointmentStatus status,
        String description,
        AppointmentEventType eventType
) {

    public static AppointmentEvent created(Appointment appointment) {
        return new AppointmentEvent(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime(),
                appointment.getStatus(),
                appointment.getDescription(),
                AppointmentEventType.CREATED
        );
    }

    public static AppointmentEvent updated(Appointment appointment) {
        return new AppointmentEvent(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getDateTime(),
                appointment.getStatus(),
                appointment.getDescription(),
                AppointmentEventType.UPDATED
        );
    }
}
