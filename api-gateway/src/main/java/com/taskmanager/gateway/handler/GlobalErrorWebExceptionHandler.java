package com.taskmanager.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global reactive exception handler formatting unhandled routing and gateway exceptions into RFC 7807 ErrorResponses.
 */
@Component
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "INTERNAL_SERVER_ERROR";
        String message = "An internal gateway error occurred. Please contact system support.";

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            errorCode = status.name();
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        } else {
            log.error("Unhandled gateway exception on path '{}': {}", exchange.getRequest().getPath().value(), ex.getMessage(), ex);
        }

        return GatewayErrorWriter.writeErrorResponse(
                exchange,
                objectMapper,
                status,
                errorCode,
                message
        );
    }

    @Override
    public int getOrder() {
        return -2; // Precedes DefaultErrorWebExceptionHandler (-1)
    }
}

