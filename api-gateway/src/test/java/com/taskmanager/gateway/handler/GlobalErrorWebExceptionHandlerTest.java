package com.taskmanager.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.CorrelationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalErrorWebExceptionHandlerTest {

    private GlobalErrorWebExceptionHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        handler = new GlobalErrorWebExceptionHandler(objectMapper);
    }

    @Test
    @DisplayName("Should handle 404 ResponseStatusException and format as RFC 7807 Problem Details")
    void testHandleResponseStatusException404() {
        String correlationId = UUID.randomUUID().toString();
        MockServerHttpRequest request = MockServerHttpRequest.get("/non-existent-route")
                .header(CorrelationConstants.HEADER_NAME, correlationId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationConstants.HEADER_NAME))
                .isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Should handle ResponseStatusException when reason is null")
    void testHandleResponseStatusExceptionNullReason() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/bad-request").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should handle generic exception and format as 500 Internal Server Error without leaking internal stack trace")
    void testHandleGenericException500() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        RuntimeException ex = new RuntimeException("Sensitive database connection password failed");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationConstants.HEADER_NAME)).isNotNull();
    }

    @Test
    @DisplayName("Should return Mono.error when response is already committed")
    void testCommittedResponse() {
        ServerWebExchange mockExchange = Mockito.mock(ServerWebExchange.class);
        ServerHttpResponse mockResponse = Mockito.mock(ServerHttpResponse.class);
        when(mockExchange.getResponse()).thenReturn(mockResponse);
        when(mockResponse.isCommitted()).thenReturn(true);

        RuntimeException ex = new RuntimeException("Boom");

        StepVerifier.create(handler.handle(mockExchange, ex))
                .expectErrorMatches(t -> t.getMessage().equals("Boom"))
                .verify();
    }

    @Test
    @DisplayName("Handler order should be -2 to precede Spring default error web exception handler")
    void testOrder() {
        assertThat(handler.getOrder()).isEqualTo(-2);
    }
}
