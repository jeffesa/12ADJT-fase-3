package com.fiap.history.domain.gateway;

import com.fiap.history.domain.entity.AppointmentHistory;

import java.util.List;
import java.util.UUID;

public interface AppointmentHistoryGateway {

    AppointmentHistory save(AppointmentHistory appointmentHistory);

    AppointmentHistory findById(UUID id);

    List<AppointmentHistory> findByPatientId(UUID patientId);

    List<AppointmentHistory> findByDoctorId(UUID doctorId);

    List<AppointmentHistory> findUpcomingByPatientId(UUID patientId);

    List<AppointmentHistory> findAll();
}
