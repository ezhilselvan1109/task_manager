package com.taskmanager.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.gateway.limiter.RateLimitResult;
import com.taskmanager.gateway.limiter.TokenBucketRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RateLimitingFilterTest {

    private TokenBucketRateLimiter rateLimiter;
    private ObjectMapper objectMapper;
    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        rateLimiter = Mockito.mock(TokenBucketRateLimiter.class);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        filter = new RateLimitingFilter(rateLimiter, objectMapper);
    }

    @Test
    @DisplayName("Should permit request and append rate limit headers when tokens are available")
    void testPermitRequest() {
        when(rateLimiter.tryAcquire(eq("auth"), eq("127.0.0.1")))
                .thenReturn(new RateLimitResult(true, 9, 10, 0));

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isTrue();
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("10");
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("9");
    }

    @Test
    @DisplayName("Should reject request with 429 Too Many Requests and Retry-After when rate limit is exceeded")
    void testRejectRequestExceedingLimit() {
        when(rateLimiter.tryAcquire(eq("auth"), eq("127.0.0.1")))
                .thenReturn(new RateLimitResult(false, 0, 10, 60));

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
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
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exchange.getResponse().getHeaders().getFirst("Retry-After")).isEqualTo("60");
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");
    }

    @Test
    @DisplayName("Should use X-User-Id header as key for protected API tier routes")
    void testProtectedApiTierKeyResolution() {
        String userId = "00000000-0000-0000-0000-000000000001";
        when(rateLimiter.tryAcquire(eq("api"), eq(userId)))
                .thenReturn(new RateLimitResult(true, 19, 20, 0));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header(SecurityConstants.HEADER_USER_ID, userId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(chainCalled.get()).isTrue();
        Mockito.verify(rateLimiter).tryAcquire(eq("api"), eq(userId));
    }

    @Test
    @DisplayName("Should rate limit user profile routes and notification routes in API tier")
    void testUserProfileAndNotificationRoutesInApiTier() {
        String userId = "00000000-0000-0000-0000-000000000002";
        when(rateLimiter.tryAcquire(eq("api"), eq(userId)))
                .thenReturn(new RateLimitResult(true, 15, 20, 0));

        // Test /api/v1/users/me
        MockServerHttpRequest userRequest = MockServerHttpRequest.get("/api/v1/users/me")
                .header(SecurityConstants.HEADER_USER_ID, userId)
                .build();
        MockServerWebExchange userExchange = MockServerWebExchange.from(userRequest);
        StepVerifier.create(filter.filter(userExchange, ex -> Mono.empty())).verifyComplete();

        // Test /api/v1/notifications
        MockServerHttpRequest notifRequest = MockServerHttpRequest.get("/api/v1/notifications")
                .header(SecurityConstants.HEADER_USER_ID, userId)
                .build();
        MockServerWebExchange notifExchange = MockServerWebExchange.from(notifRequest);
        StepVerifier.create(filter.filter(notifExchange, ex -> Mono.empty())).verifyComplete();

        Mockito.verify(rateLimiter, Mockito.times(2)).tryAcquire(eq("api"), eq(userId));
    }

    @Test
    @DisplayName("Should rate limit actuator routes using client IP")
    void testActuatorTierRateLimiting() {
        when(rateLimiter.tryAcquire(eq("actuator"), eq("192.168.1.50")))
                .thenReturn(new RateLimitResult(true, 29, 30, 0));

        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health")
                .remoteAddress(new InetSocketAddress("192.168.1.50", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty())).verifyComplete();
        Mockito.verify(rateLimiter).tryAcquire(eq("actuator"), eq("192.168.1.50"));
    }

    @Test
    @DisplayName("Should bypass rate limiting when path does not match any rate limiting tier")
    void testBypassWhenTierIsNull() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/user-service").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainCalled.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(chainCalled.get()).isTrue();
        Mockito.verifyNoInteractions(rateLimiter);
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header when present")
    void testClientIpExtractionFromForwardedFor() {
        // Multi-IP X-Forwarded-For
        when(rateLimiter.tryAcquire(eq("auth"), eq("203.0.113.195")))
                .thenReturn(new RateLimitResult(true, 9, 10, 0));

        MockServerHttpRequest requestMulti = MockServerHttpRequest.post("/api/v1/auth/register")
                .header("X-Forwarded-For", "203.0.113.195, 70.41.3.18")
                .build();
        MockServerWebExchange exchangeMulti = MockServerWebExchange.from(requestMulti);
        StepVerifier.create(filter.filter(exchangeMulti, ex -> Mono.empty())).verifyComplete();

        // Single-IP X-Forwarded-For
        when(rateLimiter.tryAcquire(eq("auth"), eq("198.51.100.1")))
                .thenReturn(new RateLimitResult(true, 8, 10, 0));

        MockServerHttpRequest requestSingle = MockServerHttpRequest.post("/api/v1/auth/register")
                .header("X-Forwarded-For", "198.51.100.1")
                .build();
        MockServerWebExchange exchangeSingle = MockServerWebExchange.from(requestSingle);
        StepVerifier.create(filter.filter(exchangeSingle, ex -> Mono.empty())).verifyComplete();

        // Fallback to 127.0.0.1 when remote address is null and X-Forwarded-For is missing
        when(rateLimiter.tryAcquire(eq("auth"), eq("127.0.0.1")))
                .thenReturn(new RateLimitResult(true, 7, 10, 0));

        MockServerHttpRequest requestNoIp = MockServerHttpRequest.post("/api/v1/auth/register").build();
        MockServerWebExchange exchangeNoIp = MockServerWebExchange.from(requestNoIp);
        StepVerifier.create(filter.filter(exchangeNoIp, ex -> Mono.empty())).verifyComplete();
    }

    @Test
    @DisplayName("Should fallback to client IP when API tier request lacks X-User-Id")
    void testApiTierWithoutUserIdFallsBackToClientIp() {
        when(rateLimiter.tryAcquire(eq("api"), eq("10.0.0.5")))
                .thenReturn(new RateLimitResult(true, 19, 20, 0));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .remoteAddress(new InetSocketAddress("10.0.0.5", 54321))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty())).verifyComplete();
        Mockito.verify(rateLimiter).tryAcquire(eq("api"), eq("10.0.0.5"));
    }

    @Test
    @DisplayName("Filter order should be -40")
    void testOrder() {
        assertThat(filter.getOrder()).isEqualTo(-40);
    }
}
