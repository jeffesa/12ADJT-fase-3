package com.fiap.scheduling.application.usecase;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.gateway.UserGateway;
import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;

import java.util.UUID;

/**
 * Caso de uso: buscar usuário por ID.
 * PATIENT só pode consultar o próprio perfil; DOCTOR/NURSE consultam qualquer um.
 */
public class FindUserByIdUseCase {

    private final UserGateway userGateway;

    public FindUserByIdUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public User execute(UUID targetUserId, UUID currentUserId, UserRole currentUserRole) {
        if (currentUserRole == UserRole.ROLE_PATIENT && !targetUserId.equals(currentUserId)) {
            throw new BusinessException("Paciente só pode consultar o próprio perfil");
        }

        return userGateway.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }
}
