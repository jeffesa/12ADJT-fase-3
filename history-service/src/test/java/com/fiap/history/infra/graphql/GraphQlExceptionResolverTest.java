package com.fiap.history.infra.graphql;

import com.fiap.history.domain.shared.BusinessException;
import com.fiap.history.domain.shared.EntityNotFoundException;
import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphQlExceptionResolverTest {

    private GraphQlExceptionResolver resolver;
    private DataFetchingEnvironment env;

    @BeforeEach
    void setUp() {
        resolver = new GraphQlExceptionResolver();
        env = mock(DataFetchingEnvironment.class);
        ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
        when(env.getExecutionStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getPath()).thenReturn(ResultPath.rootPath().segment("field"));
    }

    @Test
    @DisplayName("EntityNotFoundException → code NOT_FOUND / status 404")
    void notFound() {
        GraphQLError error = resolver.resolveToSingleError(
                new EntityNotFoundException("não encontrado"), env);

        assertThat(error).isNotNull();
        assertThat(error.getExtensions()).containsEntry("code", "NOT_FOUND");
        assertThat(error.getExtensions()).containsEntry("status", 404);
    }

    @Test
    @DisplayName("BusinessException → code BUSINESS_ERROR / status 422")
    void businessError() {
        GraphQLError error = resolver.resolveToSingleError(
                new BusinessException("regra violada"), env);

        assertThat(error).isNotNull();
        assertThat(error.getExtensions()).containsEntry("code", "BUSINESS_ERROR");
        assertThat(error.getExtensions()).containsEntry("status", 422);
    }

    @Test
    @DisplayName("AuthenticationException → code UNAUTHORIZED / status 401")
    void unauthorized() {
        AuthenticationException ex = new AuthenticationException("no auth") {
        };
        GraphQLError error = resolver.resolveToSingleError(ex, env);

        assertThat(error).isNotNull();
        assertThat(error.getExtensions()).containsEntry("code", "UNAUTHORIZED");
        assertThat(error.getExtensions()).containsEntry("status", 401);
    }

    @Test
    @DisplayName("AccessDeniedException → code FORBIDDEN / status 403")
    void forbidden() {
        GraphQLError error = resolver.resolveToSingleError(
                new AccessDeniedException("negado"), env);

        assertThat(error).isNotNull();
        assertThat(error.getExtensions()).containsEntry("code", "FORBIDDEN");
        assertThat(error.getExtensions()).containsEntry("status", 403);
    }

    @Test
    @DisplayName("Exceção desconhecida → retorna null (delega ao handler padrão)")
    void unknownReturnsNull() {
        GraphQLError error = resolver.resolveToSingleError(
                new RuntimeException("boom"), env);

        assertThat(error).isNull();
    }
}
