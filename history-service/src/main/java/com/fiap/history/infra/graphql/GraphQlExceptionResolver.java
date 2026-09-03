package com.fiap.history.infra.graphql;

import com.fiap.history.domain.shared.BusinessException;
import com.fiap.history.domain.shared.EntityNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte exceções de domínio em GraphQL errors com extensão (code/status/timestamp).
 */
@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    private static final Logger log = LoggerFactory.getLogger(GraphQlExceptionResolver.class);

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("timestamp", Instant.now().toString());

        if (ex instanceof EntityNotFoundException enf) {
            extensions.put("code", "NOT_FOUND");
            extensions.put("status", 404);
            log.warn("Entity not found: {}", enf.getMessage());
            return GraphqlErrorBuilder.newError()
                    .message(enf.getMessage())
                    .path(env.getExecutionStepInfo().getPath().toList())
                    .extensions(extensions)
                    .build();
        }

        if (ex instanceof BusinessException be) {
            extensions.put("code", "BUSINESS_ERROR");
            extensions.put("status", 422);
            log.warn("Business error: {}", be.getMessage());
            return GraphqlErrorBuilder.newError()
                    .message(be.getMessage())
                    .path(env.getExecutionStepInfo().getPath().toList())
                    .extensions(extensions)
                    .build();
        }

        if (ex instanceof AuthenticationException ae) {
            extensions.put("code", "UNAUTHORIZED");
            extensions.put("status", 401);
            log.warn("Authentication failed: {}", ae.getMessage());
            return GraphqlErrorBuilder.newError()
                    .message("Não autenticado")
                    .path(env.getExecutionStepInfo().getPath().toList())
                    .extensions(extensions)
                    .build();
        }

        if (ex instanceof AccessDeniedException ade) {
            extensions.put("code", "FORBIDDEN");
            extensions.put("status", 403);
            log.warn("Access denied: {}", ade.getMessage());
            return GraphqlErrorBuilder.newError()
                    .message("Acesso negado")
                    .path(env.getExecutionStepInfo().getPath().toList())
                    .extensions(extensions)
                    .build();
        }

        if (ex instanceof MethodArgumentNotValidException mv) {
            extensions.put("code", "BAD_REQUEST");
            extensions.put("status", 400);
            List<String> errors = mv.getBindingResult().getFieldErrors().stream()
                    .map(f -> f.getField() + ": " + f.getDefaultMessage())
                    .toList();
            extensions.put("errors", errors);
            log.warn("Validation failed: {}", errors);
            return GraphqlErrorBuilder.newError()
                    .message("Erro de validação")
                    .path(env.getExecutionStepInfo().getPath().toList())
                    .extensions(extensions)
                    .build();
        }

        // Não conhece: retorna nulo para delegar ao handler padrão do Spring GraphQL
        return null;
    }
}
