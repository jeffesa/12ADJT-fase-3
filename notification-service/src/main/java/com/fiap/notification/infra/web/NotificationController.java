package com.fiap.notification.infra.web;

import com.fiap.notification.application.usecase.FindAllNotificationsUseCase;
import com.fiap.notification.domain.entity.NotificationType;
import com.fiap.notification.domain.shared.BusinessException;
import com.fiap.notification.infra.web.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST para listagem das notificações processadas/enviadas.
 * Permite comprovar que os eventos foram consumidos pelo notification-service.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Consulta das notificações processadas pelo serviço")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final FindAllNotificationsUseCase findAllNotificationsUseCase;

    public NotificationController(FindAllNotificationsUseCase findAllNotificationsUseCase) {
        this.findAllNotificationsUseCase = findAllNotificationsUseCase;
    }

    @Operation(summary = "Lista notificações",
            description = "Retorna todas as notificações enviadas. Filtro opcional por tipo.")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> findAll(
            @Parameter(description = "Filtro opcional por tipo de notificação",
                    example = "APPOINTMENT_CREATED")
            @RequestParam(name = "type", required = false) String type) {

        NotificationType parsedType = parseType(type);

        List<NotificationResponse> response = findAllNotificationsUseCase.execute(parsedType)
                .stream()
                .map(NotificationResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    private NotificationType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return NotificationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de notificação inválido: " + type);
        }
    }
}
