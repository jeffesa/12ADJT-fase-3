package com.fiap.history.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentHistoryRepository extends JpaRepository<AppointmentHistoryJpaEntity, UUID> {

    List<AppointmentHistoryJpaEntity> findByPatientIdOrderByReceivedAtDesc(UUID patientId);

    List<AppointmentHistoryJpaEntity> findByDoctorIdOrderByReceivedAtDesc(UUID doctorId);

    List<AppointmentHistoryJpaEntity> findByPatientIdAndDateTimeAfterOrderByDateTimeAsc(UUID patientId, LocalDateTime dateTime);

    List<AppointmentHistoryJpaEntity> findAllByOrderByReceivedAtDesc();
}
