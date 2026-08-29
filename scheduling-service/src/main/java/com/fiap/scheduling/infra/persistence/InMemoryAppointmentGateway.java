package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryAppointmentGateway implements AppointmentGateway {

    private final Map<UUID, Appointment> appointments = new HashMap<>();

    @Override
    public Appointment create(Appointment appointment) {
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public Appointment update(Appointment appointment) {
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public void delete(UUID id) {
        appointments.remove(id);
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return Optional.ofNullable(appointments.get(id));
    }

    @Override
    public List<Appointment> findByPatientId(UUID patientId) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getPatientId().equals(patientId))
                .toList();
    }

    @Override
    public List<Appointment> findByDoctorId(UUID doctorId) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getDoctorId().equals(doctorId))
                .toList();
    }

    @Override
    public List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointments.values().stream()
                .filter(appointment -> !appointment.getDateTime().isBefore(start)
                        && !appointment.getDateTime().isAfter(end))
                .toList();
    }

    @Override
    public List<Appointment> findUpcoming(LocalDateTime fromDateTime) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getDateTime().isAfter(fromDateTime))
                .toList();
    }
}
