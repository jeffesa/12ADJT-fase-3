package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private UserJpaEntity patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private UserJpaEntity doctor;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AppointmentJpaEntity() {
    }

    public AppointmentJpaEntity(UUID id, UserJpaEntity patient, UserJpaEntity doctor, LocalDateTime dateTime,
                                AppointmentStatus status, String description, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentJpaEntity fromDomain(Appointment appointment, UserJpaEntity patient, UserJpaEntity doctor) {
        return new AppointmentJpaEntity(
                appointment.getId(),
                patient,
                doctor,
                appointment.getDateTime(),
                appointment.getStatus(),
                appointment.getDescription(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    public Appointment toDomain() {
        return new Appointment(
                id,
                patient.getId(),
                doctor.getId(),
                dateTime,
                status,
                description,
                createdAt,
                updatedAt
        );
    }
}
