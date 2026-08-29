package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AppointmentJpaGateway implements AppointmentGateway {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentJpaGateway(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Appointment create(Appointment appointment) {
        AppointmentJpaEntity entity = toEntity(appointment);
        return appointmentRepository.save(entity).toDomain();
    }

    @Override
    public Appointment update(Appointment appointment) {
        AppointmentJpaEntity entity = toEntity(appointment);
        return appointmentRepository.save(entity).toDomain();
    }

    @Override
    public void delete(UUID id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return appointmentRepository.findById(id).map(AppointmentJpaEntity::toDomain);
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll().stream()
                .map(AppointmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByPatientId(UUID patientId) {
        return appointmentRepository.findByPatient_Id(patientId).stream()
                .map(AppointmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByDoctorId(UUID doctorId) {
        return appointmentRepository.findByDoctor_Id(doctorId).stream()
                .map(AppointmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDateTimeBetween(start, end).stream()
                .map(AppointmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findUpcoming(LocalDateTime fromDateTime) {
        return appointmentRepository.findByDateTimeAfter(fromDateTime).stream()
                .map(AppointmentJpaEntity::toDomain)
                .toList();
    }

    private AppointmentJpaEntity toEntity(Appointment appointment) {
        UserJpaEntity patient = userRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado: " + appointment.getPatientId()));
        UserJpaEntity doctor = userRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado: " + appointment.getDoctorId()));
        return AppointmentJpaEntity.fromDomain(appointment, patient, doctor);
    }
}
