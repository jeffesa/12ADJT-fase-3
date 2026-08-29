package com.fiap.scheduling.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentJpaEntity, UUID> {

    List<AppointmentJpaEntity> findByPatient_Id(UUID patientId);

    List<AppointmentJpaEntity> findByDoctor_Id(UUID doctorId);

    List<AppointmentJpaEntity> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<AppointmentJpaEntity> findByDateTimeAfter(LocalDateTime fromDateTime);
}
