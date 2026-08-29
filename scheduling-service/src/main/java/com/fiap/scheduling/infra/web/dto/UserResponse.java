package com.fiap.scheduling.infra.web.dto;

import com.fiap.scheduling.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta para usuário.
 * NÃO expõe a senha/hash.
 */
@Schema(description = "Dados públicos de um usuário")
public record UserResponse(
        UUID id,
        String name,
        String email,
        String role,
        LocalDateTime createdAt
) {

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
