package com.fiap.history.infra.graphql;

import com.fiap.history.application.usecase.FindAllAppointmentHistoryUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByDoctorIdUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByIdUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByPatientIdUseCase;
import com.fiap.history.application.usecase.FindUpcomingAppointmentHistoryByPatientIdUseCase;
import com.fiap.history.domain.entity.AppointmentHistory;
import com.fiap.history.domain.entity.UserRole;
import com.fiap.history.domain.shared.BusinessException;
import com.fiap.history.infra.security.AuthenticatedUser;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class HistoryQueryResolver {

    private final FindAppointmentHistoryByIdUseCase findAppointmentHistoryByIdUseCase;
    private final FindAppointmentHistoryByPatientIdUseCase findAppointmentHistoryByPatientIdUseCase;
    private final FindAppointmentHistoryByDoctorIdUseCase findAppointmentHistoryByDoctorIdUseCase;
    private final FindUpcomingAppointmentHistoryByPatientIdUseCase findUpcomingAppointmentHistoryByPatientIdUseCase;
    private final FindAllAppointmentHistoryUseCase findAllAppointmentHistoryUseCase;

    public HistoryQueryResolver(FindAppointmentHistoryByIdUseCase findAppointmentHistoryByIdUseCase,
                               FindAppointmentHistoryByPatientIdUseCase findAppointmentHistoryByPatientIdUseCase,
                               FindAppointmentHistoryByDoctorIdUseCase findAppointmentHistoryByDoctorIdUseCase,
                               FindUpcomingAppointmentHistoryByPatientIdUseCase findUpcomingAppointmentHistoryByPatientIdUseCase,
                               FindAllAppointmentHistoryUseCase findAllAppointmentHistoryUseCase) {
        this.findAppointmentHistoryByIdUseCase = findAppointmentHistoryByIdUseCase;
        this.findAppointmentHistoryByPatientIdUseCase = findAppointmentHistoryByPatientIdUseCase;
        this.findAppointmentHistoryByDoctorIdUseCase = findAppointmentHistoryByDoctorIdUseCase;
        this.findUpcomingAppointmentHistoryByPatientIdUseCase = findUpcomingAppointmentHistoryByPatientIdUseCase;
        this.findAllAppointmentHistoryUseCase = findAllAppointmentHistoryUseCase;
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> appointmentsByPatient(@Argument UUID patientId) {
        AuthenticatedUser user = currentUser();
        return findAppointmentHistoryByPatientIdUseCase
                .execute(patientId, user.userId(), UserRole.fromToken(user.role()))
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> appointmentsByDoctor(@Argument UUID doctorId) {
        AuthenticatedUser user = currentUser();
        return findAppointmentHistoryByDoctorIdUseCase
                .execute(doctorId, user.userId(), UserRole.fromToken(user.role()))
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> upcomingAppointments(@Argument UUID patientId) {
        AuthenticatedUser user = currentUser();
        return findUpcomingAppointmentHistoryByPatientIdUseCase
                .execute(patientId, user.userId(), UserRole.fromToken(user.role()))
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    @QueryMapping
    public AppointmentHistoryGraphql appointmentHistory(@Argument UUID id) {
        AuthenticatedUser user = currentUser();
        AppointmentHistory appointmentHistory = findAppointmentHistoryByIdUseCase
                .execute(id, user.userId(), UserRole.fromToken(user.role()));
        return AppointmentHistoryGraphql.fromDomain(appointmentHistory);
    }

    @QueryMapping
    public List<AppointmentHistoryGraphql> allAppointmentHistories() {
        AuthenticatedUser user = currentUser();
        return findAllAppointmentHistoryUseCase
                .execute(UserRole.fromToken(user.role()))
                .stream()
                .map(AppointmentHistoryGraphql::fromDomain)
                .toList();
    }

    /**
     * Recupera o usuário autenticado do SecurityContext.
     * Base para a validação de ownership (paciente só vê seus dados).
     */
    private AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException("Usuário não autenticado");
        }
        return user;
    }
}
