package com.fiap.history.infra.config;

import com.fiap.history.application.usecase.FindAllAppointmentHistoryUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByDoctorIdUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByIdUseCase;
import com.fiap.history.application.usecase.FindAppointmentHistoryByPatientIdUseCase;
import com.fiap.history.application.usecase.FindUpcomingAppointmentHistoryByPatientIdUseCase;
import com.fiap.history.application.usecase.SaveAppointmentHistoryUseCase;
import com.fiap.history.domain.gateway.AppointmentHistoryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public SaveAppointmentHistoryUseCase saveAppointmentHistoryUseCase(AppointmentHistoryGateway appointmentHistoryGateway) {
        return new SaveAppointmentHistoryUseCase(appointmentHistoryGateway);
    }

    @Bean
    public FindAppointmentHistoryByIdUseCase findAppointmentHistoryByIdUseCase(
            AppointmentHistoryGateway appointmentHistoryGateway) {
        return new FindAppointmentHistoryByIdUseCase(appointmentHistoryGateway);
    }

    @Bean
    public FindAppointmentHistoryByPatientIdUseCase findAppointmentHistoryByPatientIdUseCase(
            AppointmentHistoryGateway appointmentHistoryGateway) {
        return new FindAppointmentHistoryByPatientIdUseCase(appointmentHistoryGateway);
    }

    @Bean
    public FindAppointmentHistoryByDoctorIdUseCase findAppointmentHistoryByDoctorIdUseCase(
            AppointmentHistoryGateway appointmentHistoryGateway) {
        return new FindAppointmentHistoryByDoctorIdUseCase(appointmentHistoryGateway);
    }

    @Bean
    public FindUpcomingAppointmentHistoryByPatientIdUseCase findUpcomingAppointmentHistoryByPatientIdUseCase(
            AppointmentHistoryGateway appointmentHistoryGateway) {
        return new FindUpcomingAppointmentHistoryByPatientIdUseCase(appointmentHistoryGateway);
    }

    @Bean
    public FindAllAppointmentHistoryUseCase findAllAppointmentHistoryUseCase(
            AppointmentHistoryGateway appointmentHistoryGateway) {
        return new FindAllAppointmentHistoryUseCase(appointmentHistoryGateway);
    }
}
