package com.taskmanager.gateway.controller;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Fallback controller providing graceful degradation responses when Resilience4j circuit breakers trip.
 * Conforms to DOC-RES-007 and DOC-API-014.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/{service}", produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    public Mono<ResponseEntity<ErrorResponse>> fallback(@PathVariable("service") String service, ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationConstants.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String path = exchange.getRequest().getPath().value();
        String message = String.format("Upstream service '%s' is temporarily unavailable. Circuit breaker fallback triggered.", service);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "SERVICE_UNAVAILABLE",
                message,
                path,
                correlationId
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
    }
}

