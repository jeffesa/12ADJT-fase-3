package com.fiap.scheduling.infra.web;

import com.fiap.scheduling.application.usecase.FindAllUsersUseCase;
import com.fiap.scheduling.application.usecase.FindUserByIdUseCase;
import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.infra.security.AuthenticatedUser;
import com.fiap.scheduling.infra.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para consulta de usuários.
 * Permite descobrir médicos, enfermeiros e pacientes cadastrados.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Consulta de usuários cadastrados")
public class UserController {

    private final FindAllUsersUseCase findAllUsersUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;

    public UserController(FindAllUsersUseCase findAllUsersUseCase,
                          FindUserByIdUseCase findUserByIdUseCase) {
        this.findAllUsersUseCase = findAllUsersUseCase;
        this.findUserByIdUseCase = findUserByIdUseCase;
    }

    @Operation(summary = "Listar usuários",
            description = "Lista todos os usuários. Filtro opcional por role (DOCTOR, NURSE, PATIENT). Apenas DOCTOR e NURSE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários"),
            @ApiResponse(responseCode = "400", description = "Role inválida"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(
            @RequestParam(name = "role", required = false) String role) {

        UserRole roleFilter = parseRoleFilter(role);
        List<User> users = findAllUsersUseCase.execute(roleFilter);
        List<UserResponse> response = users.stream()
                .map(UserResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar usuário por ID",
            description = "Retorna um usuário. PATIENT só pode consultar o próprio perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        AuthenticatedUser current = currentUser();
        User user = findUserByIdUseCase.execute(id, current.userId(), toUserRole(current.role()));
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    // ═══════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════

    private UserRole parseRoleFilter(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            // aceita tanto "PATIENT" quanto "ROLE_PATIENT"
            String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role inválida: " + role + ". Valores aceitos: DOCTOR, NURSE, PATIENT");
        }
    }

    private AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private UserRole toUserRole(String role) {
        return UserRole.valueOf(role);
    }
}
