package com.fiap.history.infra.graphql;

import com.fiap.history.application.usecase.SaveAppointmentHistoryUseCase;
import com.fiap.history.domain.entity.AppointmentHistory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Controller
public class HistoryMutationResolver {

    private final SaveAppointmentHistoryUseCase saveAppointmentHistoryUseCase;

    public HistoryMutationResolver(SaveAppointmentHistoryUseCase saveAppointmentHistoryUseCase) {
        this.saveAppointmentHistoryUseCase = saveAppointmentHistoryUseCase;
    }

    @MutationMapping
    public AppointmentHistoryGraphql saveAppointmentHistory(@Argument AppointmentHistoryInput input) {
        AppointmentHistory domain = new AppointmentHistory();
        domain.setId(UUID.randomUUID());
        domain.setAppointmentId(input.getAppointmentId());
        domain.setPatientId(input.getPatientId());
        domain.setDoctorId(input.getDoctorId());
        domain.setPatientName(input.getPatientName());
        domain.setDoctorName(input.getDoctorName());
        if (input.getDateTime() != null) {
            try {
                domain.setDateTime(LocalDateTime.parse(input.getDateTime()));
            } catch (DateTimeParseException e) {
                // ignore and leave null; GraphQlExceptionResolver will handle validation if desired
            }
        }
        domain.setStatus(input.getStatus());
        domain.setDescription(input.getDescription());
        domain.setEventType(input.getEventType());
        domain.setReceivedAt(LocalDateTime.now());

        AppointmentHistory saved = saveAppointmentHistoryUseCase.execute(domain);
        return AppointmentHistoryGraphql.fromDomain(saved);
    }
}
