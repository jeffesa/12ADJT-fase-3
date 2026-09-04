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
    private final com.fiap.history.application.usecase.FindAllAppointmentHistoryUseCase findAllAppointmentHistoryUseCase;

    public HistoryQueryResolver(FindAppointmentHistoryByIdUseCase findAppointmentHistoryByIdUseCase,
                               FindAppointmentHistoryByPatientIdUseCase findAppointmentHistoryByPatientIdUseCase,
                               FindAppointmentHistoryByDoctorIdUseCase findAppointmentHistoryByDoctorIdUseCase,
                               FindUpcomingAppointmentHistoryByPatientIdUseCase findUpcomingAppointmentHistoryByPatientIdUseCase,
                               com.fiap.history.application.usecase.FindAllAppointmentHistoryUseCase findAllAppointmentHistoryUseCase) {
        this.findAppointmentHistoryByIdUseCase = findAppointmentHistoryByIdUseCase;
        this.findAppointmentHistoryByPatientIdUseCase = findAppointmentHistoryByPatientIdUseCase;
        this.findAppointmentHistoryByDoctorIdUseCase = findAppointmentHistoryByDoctorIdUseCase;
        this.findUpcomingAppointmentHistoryByPatientIdUseCase = findUpcomingAppointmentHistoryByPatientIdUseCase;
        this.findAllAppointmentHistoryUseCase = findAllAppointmentHistoryUseCase;
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

    @QueryMapping
    public List<AppointmentHistoryGraphql> allAppointmentHistories() {
        return findAllAppointmentHistoryUseCase.execute()
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }
}

