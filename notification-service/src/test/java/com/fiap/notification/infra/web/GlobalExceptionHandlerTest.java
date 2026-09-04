package com.fiap.notification.infra.web;

import com.fiap.notification.domain.shared.BusinessException;
import com.fiap.notification.domain.shared.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.core.MethodParameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException → 422")
    void business() {
        ProblemDetail pd = handler.handleBusiness(new BusinessException("regra"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(pd.getDetail()).isEqualTo("regra");
    }

    @Test
    @DisplayName("EntityNotFoundException → 404")
    void notFound() {
        ProblemDetail pd = handler.handleNotFound(new EntityNotFoundException("sumiu"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(pd.getDetail()).isEqualTo("sumiu");
    }

    @Test
    @DisplayName("AccessDeniedException → 403")
    void accessDenied() {
        ProblemDetail pd = handler.handleAccessDenied(new AccessDeniedException("negado"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("AuthenticationException → 401")
    void authentication() {
        AuthenticationException ex = new AuthenticationException("sem auth") {
        };
        ProblemDetail pd = handler.handleAuthentication(ex);
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Exception genérica → 500")
    void general() {
        ProblemDetail pd = handler.handleGeneral(new RuntimeException("boom"));
        assertThat(pd.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → 400 com lista de erros")
    void validation() throws NoSuchMethodException {
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "campo", "não pode ser nulo"));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail pd = handler.handleValidation(ex);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(pd.getProperties()).containsKey("errors");
    }

    @SuppressWarnings("unused")
    void dummy(String param) {
    }
}
