package com.taskmanager.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.error.ErrorResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utility for writing standardized RFC 7807 ErrorResponse payloads into reactive ServerHttpResponse streams.
 */
public final class GatewayErrorWriter {

    private GatewayErrorWriter() {
    }

    /**
     * Serializes and writes an RFC 7807 problem details JSON response.
     */
    public static Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            ObjectMapper objectMapper,
            HttpStatus status,
            String errorCode,
            String message) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = response.getHeaders().getFirst(CorrelationConstants.HEADER_NAME);
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        response.getHeaders().set(CorrelationConstants.HEADER_NAME, correlationId);

        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                exchange.getRequest().getPath().value(),
                correlationId
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            bytes = "{}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}

