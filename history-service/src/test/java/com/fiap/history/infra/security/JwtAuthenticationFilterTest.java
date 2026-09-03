package com.fiap.history.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenValidator tokenValidator;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenValidator);
    }

    @AfterEach
    void tearDown() {
        // clear security context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPopulateSecurityContextWhenTokenValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenValidator.validateToken(token)).thenReturn(true);
        when(tokenValidator.getEmailFromToken(token)).thenReturn("user@example.com");
        when(tokenValidator.getRoleFromToken(token)).thenReturn("ROLE_PATIENT");
        when(tokenValidator.getUserIdFromToken(token)).thenReturn(UUID.randomUUID());

        filter.doFilterInternal(request, response, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals("ROLE_PATIENT", auth.getAuthorities().stream().findFirst().get().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotPopulateSecurityContextWhenTokenInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenValidator.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(request, response);
    }
}
