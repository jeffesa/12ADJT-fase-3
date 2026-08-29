package com.fiap.scheduling.infra.web;

import com.fiap.scheduling.application.usecase.CancelAppointmentUseCase;
import com.fiap.scheduling.application.usecase.CreateAppointmentUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentByIdUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentsByDoctorUseCase;
import com.fiap.scheduling.application.usecase.FindAppointmentsByPatientUseCase;
import com.fiap.scheduling.application.usecase.FindUpcomingAppointmentsUseCase;
import com.fiap.scheduling.application.usecase.UpdateAppointmentUseCase;
import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.UserRole;
import com.fiap.scheduling.infra.security.AuthenticatedUser;
import com.fiap.scheduling.infra.web.dto.AppointmentResponse;
import com.fiap.scheduling.infra.web.dto.CreateAppointmentRequest;
import com.fiap.scheduling.infra.web.dto.UpdateAppointmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para gerenciamento de consultas.
 * Endpoints protegidos por role (validação em SecurityConfig + use cases).
 */
@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Gerenciamento de consultas médicas")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final UpdateAppointmentUseCase updateAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final FindAppointmentByIdUseCase findAppointmentByIdUseCase;
    private final FindAppointmentsByPatientUseCase findAppointmentsByPatientUseCase;
    private final FindAppointmentsByDoctorUseCase findAppointmentsByDoctorUseCase;
    private final FindUpcomingAppointmentsUseCase findUpcomingAppointmentsUseCase;

    public AppointmentController(CreateAppointmentUseCase createAppointmentUseCase,
                                 UpdateAppointmentUseCase updateAppointmentUseCase,
                                 CancelAppointmentUseCase cancelAppointmentUseCase,
                                 FindAppointmentByIdUseCase findAppointmentByIdUseCase,
                                 FindAppointmentsByPatientUseCase findAppointmentsByPatientUseCase,
                                 FindAppointmentsByDoctorUseCase findAppointmentsByDoctorUseCase,
                                 FindUpcomingAppointmentsUseCase findUpcomingAppointmentsUseCase) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.updateAppointmentUseCase = updateAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
        this.findAppointmentByIdUseCase = findAppointmentByIdUseCase;
        this.findAppointmentsByPatientUseCase = findAppointmentsByPatientUseCase;
        this.findAppointmentsByDoctorUseCase = findAppointmentsByDoctorUseCase;
        this.findUpcomingAppointmentsUseCase = findUpcomingAppointmentsUseCase;
    }

    @Operation(summary = "Criar consulta", description = "Cria uma nova consulta. Apenas DOCTOR e NURSE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consulta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Paciente ou médico não encontrado"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        AuthenticatedUser user = currentUser();
        Appointment appointment = createAppointmentUseCase.execute(
                request.patientId(),
                request.doctorId(),
                request.dateTime(),
                request.description(),
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.fromDomain(appointment));
    }

    @Operation(summary = "Atualizar consulta", description = "Edita uma consulta existente. Apenas DOCTOR e NURSE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateAppointmentRequest request) {
        AuthenticatedUser user = currentUser();
        Appointment appointment = updateAppointmentUseCase.execute(
                id,
                request.patientId(),
                request.doctorId(),
                request.dateTime(),
                request.description(),
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.ok(AppointmentResponse.fromDomain(appointment));
    }

    @Operation(summary = "Cancelar consulta",
            description = "Cancela uma consulta. DOCTOR, NURSE ou o próprio PATIENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta cancelada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable UUID id) {
        AuthenticatedUser user = currentUser();
        Appointment appointment = cancelAppointmentUseCase.execute(
                id,
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.ok(AppointmentResponse.fromDomain(appointment));
    }

    @Operation(summary = "Buscar consulta por ID",
            description = "Retorna uma consulta. PATIENT só acessa as suas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable UUID id) {
        AuthenticatedUser user = currentUser();
        Appointment appointment = findAppointmentByIdUseCase.execute(
                id,
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.ok(AppointmentResponse.fromDomain(appointment));
    }

    @Operation(summary = "Listar consultas por paciente",
            description = "DOCTOR, NURSE ou o próprio PATIENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de consultas"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> findByPatient(@PathVariable UUID patientId) {
        AuthenticatedUser user = currentUser();
        List<Appointment> appointments = findAppointmentsByPatientUseCase.execute(
                patientId,
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.ok(toResponseList(appointments));
    }

    @Operation(summary = "Listar consultas por médico", description = "DOCTOR ou NURSE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de consultas"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> findByDoctor(@PathVariable UUID doctorId) {
        AuthenticatedUser user = currentUser();
        List<Appointment> appointments = findAppointmentsByDoctorUseCase.execute(
                doctorId,
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.ok(toResponseList(appointments));
    }

    @Operation(summary = "Listar consultas futuras",
            description = "Consultas futuras filtradas por role do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de consultas futuras")
    })
    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentResponse>> findUpcoming() {
        AuthenticatedUser user = currentUser();
        List<Appointment> appointments = findUpcomingAppointmentsUseCase.execute(
                user.userId(),
                toUserRole(user.role())
        );
        return ResponseEntity.ok(toResponseList(appointments));
    }

    // ═══════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════

    private AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private UserRole toUserRole(String role) {
        return UserRole.valueOf(role);
    }

    private List<AppointmentResponse> toResponseList(List<Appointment> appointments) {
        return appointments.stream()
                .map(AppointmentResponse::fromDomain)
                .toList();
    }
}
