package com.fiap.scheduling.infra.config;

import com.fiap.scheduling.application.usecase.CancelAppointmentUseCase;
import com.fiap.scheduling.application.usecase.CreateAppointmentUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentByIdUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentsByDoctorUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentsByPatientUseCase;
import com.fiap.scheduling.application.usecase.FindUpcomingAppointmentsUseCase;
import com.fiap.scheduling.application.usecase.LoginUseCase;
import com.fiap.scheduling.application.usecase.RegisterUserUseCase;
import com.fiap.scheduling.application.usecase.UpdateAppointmentUseCase;
import com.fiap.scheduling.domain.event.EventPublisher;
import com.fiap.scheduling.domain.gateway.AppointmentGateway;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.PasswordHasher;
import com.fiap.scheduling.domain.shared.TokenProvider;
import com.fiap.scheduling.infra.messaging.RabbitEventPublisher;
import com.fiap.scheduling.infra.persistence.InMemoryAppointmentGateway;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de beans para use cases.
 * Use cases são POJOs puros — registrados aqui como Spring beans.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserGateway userGateway, PasswordHasher passwordHasher) {
        return new RegisterUserUseCase(userGateway, passwordHasher);
    }

    @Bean
    public LoginUseCase loginUseCase(UserGateway userGateway, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        return new LoginUseCase(userGateway, passwordHasher, tokenProvider);
    }

    @Bean
    @ConditionalOnMissingBean(AppointmentGateway.class)
    public AppointmentGateway appointmentGateway() {
        return new InMemoryAppointmentGateway();
    }

    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public EventPublisher rabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisher(rabbitTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher eventPublisher() {
        return event -> {
        };
    }

    @Bean
    public CreateAppointmentUseCase createAppointmentUseCase(AppointmentGateway appointmentGateway,
                                                            UserGateway userGateway,
                                                            EventPublisher eventPublisher) {
        return new CreateAppointmentUseCase(appointmentGateway, userGateway, eventPublisher);
    }

    @Bean
    public UpdateAppointmentUseCase updateAppointmentUseCase(AppointmentGateway appointmentGateway,
                                                            UserGateway userGateway,
                                                            EventPublisher eventPublisher) {
        return new UpdateAppointmentUseCase(appointmentGateway, userGateway, eventPublisher);
    }

    @Bean
    public CancelAppointmentUseCase cancelAppointmentUseCase(AppointmentGateway appointmentGateway) {
        return new CancelAppointmentUseCase(appointmentGateway);
    }

    @Bean
    public FindAppointmentByIdUseCase findAppointmentByIdUseCase(AppointmentGateway appointmentGateway) {
        return new FindAppointmentByIdUseCase(appointmentGateway);
    }

    @Bean
    public FindAppointmentsByPatientUseCase findAppointmentsByPatientUseCase(AppointmentGateway appointmentGateway) {
        return new FindAppointmentsByPatientUseCase(appointmentGateway);
    }

    @Bean
    public FindAppointmentsByDoctorUseCase findAppointmentsByDoctorUseCase(AppointmentGateway appointmentGateway) {
        return new FindAppointmentsByDoctorUseCase(appointmentGateway);
    }

    @Bean
    public FindUpcomingAppointmentsUseCase findUpcomingAppointmentsUseCase(AppointmentGateway appointmentGateway) {
        return new FindUpcomingAppointmentsUseCase(appointmentGateway);
    }
}
