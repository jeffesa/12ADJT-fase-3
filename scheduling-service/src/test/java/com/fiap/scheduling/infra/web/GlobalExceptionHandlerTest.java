package com.fiap.scheduling.infra.web;

import com.fiap.scheduling.domain.shared.BusinessException;
import com.fiap.scheduling.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("EntityNotFoundException → 404")
    void notFound() {
        ProblemDetail pd = handler.handleNotFound(new EntityNotFoundException("não achou"));
        assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
        assertEquals("não achou", pd.getDetail());
    }

    @Test
    @DisplayName("BusinessException → 422")
    void business() {
        ProblemDetail pd = handler.handleBusiness(new BusinessException("regra violada"));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), pd.getStatus());
        assertEquals("regra violada", pd.getDetail());
    }

    @Test
    @DisplayName("AccessDeniedException → 403")
    void accessDenied() {
        ProblemDetail pd = handler.handleAccessDenied(new AccessDeniedException("negado"));
        assertEquals(HttpStatus.FORBIDDEN.value(), pd.getStatus());
    }

    @Test
    @DisplayName("AuthenticationException → 401")
    void authentication() {
        ProblemDetail pd = handler.handleAuthentication(new AuthenticationException("nao auth") {});
        assertEquals(HttpStatus.UNAUTHORIZED.value(), pd.getStatus());
    }

    @Test
    @DisplayName("IllegalArgumentException → 400")
    void illegalArgument() {
        ProblemDetail pd = handler.handleIllegalArgument(new IllegalArgumentException("arg ruim"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("arg ruim", pd.getDetail());
    }

    @Test
    @DisplayName("Exception genérica → 500")
    void generic() {
        ProblemDetail pd = handler.handleGeneral(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertNotNull(pd.getDetail());
    }
}
