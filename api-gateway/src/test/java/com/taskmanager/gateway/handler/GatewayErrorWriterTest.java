package com.taskmanager.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.CorrelationConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GatewayErrorWriterTest {

    @Test
    @DisplayName("Should extract correlation ID from response header when absent on request")
    void testExtractCorrelationIdFromResponseHeader() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        String correlationId = UUID.randomUUID().toString();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().getHeaders().set(CorrelationConstants.HEADER_NAME, correlationId);

        StepVerifier.create(GatewayErrorWriter.writeErrorResponse(
                exchange,
                objectMapper,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Unauthorized access"
        )).verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationConstants.HEADER_NAME))
                .isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Should generate fresh correlation ID when absent on both request and response")
    void testGenerateCorrelationIdWhenMissingEverywhere() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(GatewayErrorWriter.writeErrorResponse(
                exchange,
                objectMapper,
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Forbidden access"
        )).verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationConstants.HEADER_NAME))
                .isNotNull();
    }

    @Test
    @DisplayName("Should handle JSON serialization failure gracefully")
    void testSerializationFailureFallback() throws Exception {
        ObjectMapper mockMapper = Mockito.mock(ObjectMapper.class);
        when(mockMapper.writeValueAsBytes(any()))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/tasks").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(GatewayErrorWriter.writeErrorResponse(
                exchange,
                mockMapper,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Internal error occurred"
        )).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

