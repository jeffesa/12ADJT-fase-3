package com.fiap.history.infra.persistence;

import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class AppointmentHistoryJpaGateway implements AppointmentHistoryGateway {

    private final AppointmentHistoryRepository appointmentHistoryRepository;

    public AppointmentHistoryJpaGateway(AppointmentHistoryRepository appointmentHistoryRepository) {
        this.appointmentHistoryRepository = appointmentHistoryRepository;
    }

    @Override
    public AppointmentHistory save(AppointmentHistory appointmentHistory) {
        AppointmentHistoryJpaEntity entity = AppointmentHistoryJpaEntity.fromDomain(appointmentHistory);
        return appointmentHistoryRepository.save(entity).toDomain();
    }

    @Override
    public List<AppointmentHistory> findByPatientId(UUID patientId) {
        return appointmentHistoryRepository.findByPatientIdOrderByReceivedAtDesc(patientId)
                .stream()
                .map(AppointmentHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<AppointmentHistory> findByDoctorId(UUID doctorId) {
        return appointmentHistoryRepository.findByDoctorIdOrderByReceivedAtDesc(doctorId)
                .stream()
                .map(AppointmentHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<AppointmentHistory> findUpcomingByPatientId(UUID patientId) {
        return appointmentHistoryRepository.findByPatientIdAndDateTimeAfterOrderByDateTimeAsc(patientId, LocalDateTime.now())
                .stream()
                .map(AppointmentHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<AppointmentHistory> findAll() {
        return appointmentHistoryRepository.findAllByOrderByReceivedAtDesc()
                .stream()
                .map(AppointmentHistoryJpaEntity::toDomain)
                .toList();
    }
}
