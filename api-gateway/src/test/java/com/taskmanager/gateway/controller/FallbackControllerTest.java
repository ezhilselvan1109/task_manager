package com.taskmanager.gateway.controller;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.error.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackControllerTest {

    private FallbackController controller;

    @BeforeEach
    void setUp() {
        controller = new FallbackController();
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable for user-service fallback with RFC 7807 payload")
    void testUserServiceFallback() {
        String correlationId = UUID.randomUUID().toString();
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/user-service")
                .header(CorrelationConstants.HEADER_NAME, correlationId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<ResponseEntity<ErrorResponse>> result = controller.fallback("user-service", exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    ErrorResponse body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.status()).isEqualTo(503);
                    assertThat(body.error()).isEqualTo("Service Unavailable");
                    assertThat(body.errorCode()).isEqualTo("SERVICE_UNAVAILABLE");
                    assertThat(body.correlationId()).isEqualTo(correlationId);
                    assertThat(body.path()).isEqualTo("/fallback/user-service");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable for task-service fallback when correlation ID is missing")
    void testTaskServiceFallback() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/task-service").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<ResponseEntity<ErrorResponse>> result = controller.fallback("task-service", exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().errorCode()).isEqualTo("SERVICE_UNAVAILABLE");
                    assertThat(response.getBody().correlationId()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable for notification-service fallback when correlation ID is blank")
    void testNotificationServiceFallback() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/notification-service")
                .header(CorrelationConstants.HEADER_NAME, "   ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<ResponseEntity<ErrorResponse>> result = controller.fallback("notification-service", exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().errorCode()).isEqualTo("SERVICE_UNAVAILABLE");
                    assertThat(response.getBody().correlationId()).isNotBlank();
                })
                .verifyComplete();
    }
}
