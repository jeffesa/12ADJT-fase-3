package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentJpaEntity, UUID> {

    List<AppointmentJpaEntity> findByPatient_Id(UUID patientId);

    List<AppointmentJpaEntity> findByDoctor_Id(UUID doctorId);

    List<AppointmentJpaEntity> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<AppointmentJpaEntity> findByDateTimeAfter(LocalDateTime fromDateTime);

    /**
     * Consultas elegíveis a lembrete: com um dos status informados, dateTime dentro
     * da janela [start, end] e que ainda não tiveram lembrete enviado.
     */
    List<AppointmentJpaEntity> findByStatusInAndDateTimeBetweenAndReminderSentFalse(
            Collection<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);
}
