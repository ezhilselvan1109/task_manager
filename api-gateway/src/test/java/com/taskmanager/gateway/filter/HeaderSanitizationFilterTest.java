package com.taskmanager.gateway.filter;

import com.taskmanager.common.constants.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderSanitizationFilterTest {

    private HeaderSanitizationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HeaderSanitizationFilter();
    }

    @Test
    @DisplayName("Should strip client-supplied identity headers before forwarding")
    void testStripClientIdentityHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                .header(SecurityConstants.HEADER_USER_ID, "00000000-0000-0000-0000-000000000001")
                .header(SecurityConstants.HEADER_USER_ROLE, "ROLE_ADMIN")
                .header(SecurityConstants.HEADER_USER_NAME, "admin@attacker.com")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<HttpHeaders> forwardedHeaders = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            forwardedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(forwardedHeaders.get()).isNotNull();
        assertThat(forwardedHeaders.get().getFirst(SecurityConstants.HEADER_USER_ID)).isNull();
        assertThat(forwardedHeaders.get().getFirst(SecurityConstants.HEADER_USER_ROLE)).isNull();
        assertThat(forwardedHeaders.get().getFirst(SecurityConstants.HEADER_USER_NAME)).isNull();
        assertThat(forwardedHeaders.get().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer some-token");
        assertThat(forwardedHeaders.get().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Filter order should be -100")
    void testOrder() {
        assertThat(filter.getOrder()).isEqualTo(-100);
    }
}

