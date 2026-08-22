package com.fiap.scheduling.infra.web;

import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Tratamento global de erros seguindo RFC 7807 (ProblemDetail).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Erro de validação");
        problem.setType(URI.create("https://api.fiap.com/errors/validation"));

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .toList();

        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());

        log.warn("Validação falhou: {}", errors);
        return problem;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Recurso não encontrado");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.fiap.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Entidade não encontrada: {}", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Erro de negócio");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.fiap.com/errors/business"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Erro de negócio: {}", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Acesso negado");
        problem.setDetail("Você não tem permissão para acessar este recurso");
        problem.setType(URI.create("https://api.fiap.com/errors/forbidden"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Acesso negado: {}", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Não autenticado");
        problem.setDetail("Autenticação necessária para acessar este recurso");
        problem.setType(URI.create("https://api.fiap.com/errors/unauthorized"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Autenticação falhou: {}", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Argumento inválido");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.fiap.com/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now());

        log.warn("Argumento inválido: {}", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Erro interno");
        problem.setDetail("Ocorreu um erro inesperado. Tente novamente mais tarde.");
        problem.setType(URI.create("https://api.fiap.com/errors/internal"));
        problem.setProperty("timestamp", Instant.now());

        log.error("Erro interno não tratado: ", ex);
        return problem;
    }
}
