package com.fiap.history.infra.graphql;

import com.fiap.history.application.usecase.FindAppointmentHistoryByDoctorIdUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByIdUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByPatientIdUseCase;
import com.fiap.history.application.usecase.FindUpcomingAppointmentHistoryByPatientIdUseCase;
import com.fiap.history.domain.entity.AppointmentHistory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class HistoryQueryResolver {

    private final FindAppointmentHistoryByIdUseCase findAppointmentHistoryByIdUseCase;
    private final FindAppointmentHistoryByPatientIdUseCase findAppointmentHistoryByPatientIdUseCase;
    private final FindAppointmentHistoryByDoctorIdUseCase findAppointmentHistoryByDoctorIdUseCase;
    private final FindUpcomingAppointmentHistoryByPatientIdUseCase findUpcomingAppointmentHistoryByPatientIdUseCase;

    public HistoryQueryResolver(FindAppointmentHistoryByIdUseCase findAppointmentHistoryByIdUseCase,
                               FindAppointmentHistoryByPatientIdUseCase findAppointmentHistoryByPatientIdUseCase,
                               FindAppointmentHistoryByDoctorIdUseCase findAppointmentHistoryByDoctorIdUseCase,
                               FindUpcomingAppointmentHistoryByPatientIdUseCase findUpcomingAppointmentHistoryByPatientIdUseCase) {
        this.findAppointmentHistoryByIdUseCase = findAppointmentHistoryByIdUseCase;
        this.findAppointmentHistoryByPatientIdUseCase = findAppointmentHistoryByPatientIdUseCase;
        this.findAppointmentHistoryByDoctorIdUseCase = findAppointmentHistoryByDoctorIdUseCase;
        this.findUpcomingAppointmentHistoryByPatientIdUseCase = findUpcomingAppointmentHistoryByPatientIdUseCase;
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> appointmentsByPatient(@Argument UUID patientId) {
        return findAppointmentHistoryByPatientIdUseCase.execute(patientId)
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> appointmentsByDoctor(@Argument UUID doctorId) {
        return findAppointmentHistoryByDoctorIdUseCase.execute(doctorId)
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> upcomingAppointments(@Argument UUID patientId) {
        return findUpcomingAppointmentHistoryByPatientIdUseCase.execute(patientId)
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    @QueryMapping
    public AppointmentHistoryGraphql appointmentHistory(@Argument UUID id) {
        AppointmentHistory appointmentHistory = findAppointmentHistoryByIdUseCase.execute(id);
        return AppointmentHistoryGraphql.fromDomain(appointmentHistory);
    }
}
