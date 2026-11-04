package com.taskmanager.gateway.filter;

import com.taskmanager.common.constants.CorrelationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
    }

    @Test
    @DisplayName("Should preserve existing valid correlation ID and propagate downstream and in response")
    void testExistingValidCorrelationId() {
        String existingId = UUID.randomUUID().toString();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header(CorrelationConstants.HEADER_NAME, existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(downstreamHeader.get()).isEqualTo(existingId);
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationConstants.HEADER_NAME)).isEqualTo(existingId);
    }

    @Test
    @DisplayName("Should support X-Correlation-Id alternate header casing")
    void testAlternateHeaderCasing() {
        String alternateId = "custom-trace-id-12345";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header("X-Correlation-Id", alternateId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(downstreamHeader.get()).isEqualTo(alternateId);
    }

    @Test
    @DisplayName("Should generate a valid UUID when correlation ID is missing")
    void testMissingCorrelationId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(downstreamHeader.get()).isNotNull();
        UUID generatedUuid = UUID.fromString(downstreamHeader.get());
        assertThat(generatedUuid).isNotNull();
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationConstants.HEADER_NAME))
                .isEqualTo(downstreamHeader.get());
    }

    @Test
    @DisplayName("Should replace blank or malformed correlation ID with newly generated UUID")
    void testMalformedOrBlankCorrelationId() {
        // Malformed with injection chars
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks")
                .header(CorrelationConstants.HEADER_NAME, "bad\r\nheader<injection>")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<String> downstreamHeader = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstreamHeader.set(ex.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(downstreamHeader.get()).isNotEqualTo("bad\r\nheader<injection>");
        UUID generatedUuid = UUID.fromString(downstreamHeader.get());
        assertThat(generatedUuid).isNotNull();

        // Blank header
        MockServerHttpRequest requestBlank = MockServerHttpRequest.get("/api/v1/tasks")
                .header(CorrelationConstants.HEADER_NAME, "   ")
                .build();
        MockServerWebExchange exchangeBlank = MockServerWebExchange.from(requestBlank);
        AtomicReference<String> blankDownstream = new AtomicReference<>();
        StepVerifier.create(filter.filter(exchangeBlank, ex -> {
            blankDownstream.set(ex.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME));
            return Mono.empty();
        })).verifyComplete();

        assertThat(blankDownstream.get()).isNotBlank();
    }

    @Test
    @DisplayName("Filter order should be -200")
    void testOrder() {
        assertThat(filter.getOrder()).isEqualTo(-200);
    }
}
