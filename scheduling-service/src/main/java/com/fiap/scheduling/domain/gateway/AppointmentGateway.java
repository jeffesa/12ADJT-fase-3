package com.fiap.scheduling.domain.gateway;

import com.fiap.scheduling.domain.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de gateway para persistência de consultas.
 * Implementação na camada de infraestrutura.
 */
public interface AppointmentGateway {

    Appointment create(Appointment appointment);

    Appointment update(Appointment appointment);

    void delete(UUID id);

    Optional<Appointment> findById(UUID id);

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByDoctorId(UUID doctorId);

    List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end);

    List<Appointment> findUpcoming(LocalDateTime fromDateTime);
}
