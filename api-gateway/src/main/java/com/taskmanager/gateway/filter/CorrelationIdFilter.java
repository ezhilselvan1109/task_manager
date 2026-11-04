package com.taskmanager.gateway.filter;

import com.taskmanager.common.constants.CorrelationConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Perimeter filter guaranteeing every request and response carries a valid X-Correlation-ID header.
 * Sanitizes untrusted client input and generates standard UUIDs when missing or malformed.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Pattern SAFE_CORRELATION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = extractValidCorrelationId(exchange.getRequest());

        // Reflect on client response header
        exchange.getResponse().getHeaders().set(CorrelationConstants.HEADER_NAME, correlationId);

        // Inject into downstream request header
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CorrelationConstants.HEADER_NAME, correlationId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private String extractValidCorrelationId(ServerHttpRequest request) {
        String incomingId = request.getHeaders().getFirst(CorrelationConstants.HEADER_NAME);
        if (incomingId == null || incomingId.isBlank()) {
            incomingId = request.getHeaders().getFirst("X-Correlation-Id");
        }

        if (incomingId != null && !incomingId.isBlank()) {
            String trimmed = incomingId.trim();
            if (SAFE_CORRELATION_ID_PATTERN.matcher(trimmed).matches()) {
                return trimmed;
            }
        }

        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return -200; // First filter in chain
    }
}

