package com.taskmanager.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.gateway.security.JwtValidationResult;
import com.taskmanager.gateway.security.JwtValidator;
import com.taskmanager.gateway.security.UserIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtValidator jwtValidator;
    private ObjectMapper objectMapper;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtValidator = Mockito.mock(JwtValidator.class);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        filter = new JwtAuthenticationFilter(jwtValidator, objectMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "/fallback/task-service"
    })
    @DisplayName("Should bypass authentication for public and whitelisted routes")
    void testPublicRoutesBypassAuthentication(String path) {
        MockServerHttpRequest request = MockServerHttpRequest.post(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    @DisplayName("Should reject protected route when Authorization header is missing")
    void testMissingAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject protected route when Authorization header does not start with Bearer")
    void testInvalidAuthorizationHeaderPrefix() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should reject protected route when token validation fails")
    void testInvalidTokenRejection() {
        when(jwtValidator.validate(anyString()))
                .thenReturn(JwtValidationResult.failure("EXPIRED_TOKEN", "JWT access token has expired"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer expired-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should successfully authenticate valid token and inject trusted identity headers downstream")
    void testValidTokenAuthenticationAndHeaderInjection() {
        UUID userId = UUID.randomUUID();
        UserIdentity identity = new UserIdentity(userId, "ROLE_USER", "user@taskmanager.com");

        when(jwtValidator.validate("valid-access-token"))
                .thenReturn(JwtValidationResult.success(identity));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-access-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<HttpHeaders> downstreamHeaders = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(downstreamHeaders.get()).isNotNull();
        assertThat(downstreamHeaders.get().getFirst(SecurityConstants.HEADER_USER_ID))
                .isEqualTo(userId.toString());
        assertThat(downstreamHeaders.get().getFirst(SecurityConstants.HEADER_USER_ROLE))
                .isEqualTo("ROLE_USER");
        assertThat(downstreamHeaders.get().getFirst(SecurityConstants.HEADER_USER_NAME))
                .isEqualTo("user@taskmanager.com");
    }

    @Test
    @DisplayName("Filter order should be -50")
    void testOrder() {
        assertThat(filter.getOrder()).isEqualTo(-50);
    }
}

