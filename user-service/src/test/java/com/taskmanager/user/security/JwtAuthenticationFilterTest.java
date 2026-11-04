package com.taskmanager.user.security;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Filter extracts and establishes authentication from valid Bearer token")
    void testFilterWithValidBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID userId = UUID.randomUUID();

        request.addHeader(CorrelationConstants.HEADER_NAME, "corr-1234");
        request.addHeader(SecurityConstants.AUTH_HEADER, "Bearer valid.jwt.token");

        when(jwtTokenProvider.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtTokenProvider.extractUserId("valid.jwt.token")).thenReturn(userId);
        when(jwtTokenProvider.extractEmail("valid.jwt.token")).thenReturn("user@example.com");
        when(jwtTokenProvider.extractRole("valid.jwt.token")).thenReturn("ROLE_USER");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
        assertThat(response.getHeader(CorrelationConstants.HEADER_NAME)).isEqualTo("corr-1234");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter handles Bearer token with null role gracefully")
    void testFilterWithBearerTokenNullRole() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID userId = UUID.randomUUID();

        request.addHeader(SecurityConstants.AUTH_HEADER, "Bearer token.with.null.role");

        when(jwtTokenProvider.validateToken("token.with.null.role")).thenReturn(true);
        when(jwtTokenProvider.extractUserId("token.with.null.role")).thenReturn(userId);
        when(jwtTokenProvider.extractEmail("token.with.null.role")).thenReturn("user@example.com");
        when(jwtTokenProvider.extractRole("token.with.null.role")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter extracts and establishes authentication from Gateway identity headers")
    void testFilterWithGatewayHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID userId = UUID.randomUUID();

        request.addHeader(SecurityConstants.HEADER_USER_ID, userId.toString());
        request.addHeader(SecurityConstants.HEADER_USER_ROLE, "ROLE_USER");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
        assertThat(response.getHeader(CorrelationConstants.HEADER_NAME)).isNotBlank();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter handles Gateway identity header with null role")
    void testFilterWithGatewayHeaderNullRole() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UUID userId = UUID.randomUUID();

        request.addHeader(SecurityConstants.HEADER_USER_ID, userId.toString());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter ignores non-Bearer Authorization headers")
    void testFilterWithNonBearerAuthHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(SecurityConstants.AUTH_HEADER, "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter generates correlation ID when missing and handles no auth headers")
    void testFilterWithoutHeaders() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getHeader(CorrelationConstants.HEADER_NAME)).isNotBlank();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter ignores invalid token and continues filter chain without authenticating")
    void testFilterWithInvalidToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(SecurityConstants.AUTH_HEADER, "Bearer invalid.token");
        when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter gracefully handles malformed Gateway UUID header")
    void testFilterWithMalformedGatewayHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(SecurityConstants.HEADER_USER_ID, "not-a-valid-uuid");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
