package com.fiap.scheduling.infra.web;

import com.fiap.scheduling.application.usecase.LoginUseCase;
import com.fiap.scheduling.application.usecase.RegisterUserUseCase;
import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.domain.shared.TokenProvider;
import com.fiap.scheduling.infra.web.dto.AuthResponse;
import com.fiap.scheduling.infra.web.dto.LoginRequest;
import com.fiap.scheduling.infra.web.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de autenticação.
 * Endpoints públicos para registro e login.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Endpoints de autenticação (registro e login)")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final TokenProvider tokenProvider;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUseCase loginUseCase,
                          TokenProvider tokenProvider) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Registrar novo usuário",
            description = "Cria um novo usuário com role (DOCTOR, NURSE, PATIENT) e retorna JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "422", description = "Email já cadastrado")
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserRole role = UserRole.valueOf("ROLE_" + request.role().name());

        User user = registerUserUseCase.execute(
                request.name(),
                request.email(),
                request.password(),
                role
        );

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login",
            description = "Autentica o usuário e retorna JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "422", description = "Email ou senha inválidos")
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUseCase.LoginResult result = loginUseCase.execute(request.email(), request.password());

        AuthResponse response = new AuthResponse(
                result.token(),
                result.user().getId(),
                result.user().getName(),
                result.user().getEmail(),
                result.user().getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}
