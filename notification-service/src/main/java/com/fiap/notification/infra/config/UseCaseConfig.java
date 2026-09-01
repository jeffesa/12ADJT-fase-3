package com.fiap.notification.infra.config;

import com.fiap.notification.application.usecase.ProcessAppointmentEventUseCase;
import com.fiap.notification.application.usecase.SendNotificationUseCase;
import com.fiap.notification.domain.gateway.NotificationGateway;
import com.fiap.notification.domain.gateway.NotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de beans dos use cases (POJOs puros do application layer).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public ProcessAppointmentEventUseCase processAppointmentEventUseCase(
            NotificationSender notificationSender,
            NotificationGateway notificationGateway) {
        return new SendNotificationUseCase(notificationSender, notificationGateway);
    }
}
