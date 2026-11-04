package com.taskmanager.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.gateway.handler.GatewayErrorWriter;
import com.taskmanager.gateway.limiter.RateLimitResult;
import com.taskmanager.gateway.limiter.TokenBucketRateLimiter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Perimeter rate-limiting filter applying token-bucket limits per route tier and partition key.
 * Conforms strictly to DOC-RES-005 without external Redis dependencies.
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final TokenBucketRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(TokenBucketRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String tier = resolveTier(path);

        if (tier == null) {
            return chain.filter(exchange);
        }

        String key = resolveKey(tier, exchange);
        RateLimitResult result = rateLimiter.tryAcquire(tier, key);

        if (result.allowed()) {
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().set("X-RateLimit-Limit", String.valueOf(result.limit()));
            response.getHeaders().set("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
            return chain.filter(exchange);
        }

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set("Retry-After", String.valueOf(result.retryAfterSeconds()));
        response.getHeaders().set("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.getHeaders().set("X-RateLimit-Remaining", "0");

        String message = "Rate limit exceeded. Maximum " + result.limit() + " requests per minute permitted.";
        return GatewayErrorWriter.writeErrorResponse(
                exchange,
                objectMapper,
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                message
        );
    }

    private String resolveTier(String path) {
        if (path.startsWith("/api/v1/auth/")) {
            return "auth";
        } else if (path.startsWith("/api/v1/tasks") || path.startsWith("/api/v1/notifications") || path.startsWith("/api/v1/users")) {
            return "api";
        } else if (path.startsWith("/actuator/")) {
            return "actuator";
        }
        return null;
    }

    private String resolveKey(String tier, ServerWebExchange exchange) {
        if ("api".equals(tier)) {
            String userId = exchange.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                return userId;
            }
        }
        return resolveClientIp(exchange);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex > 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor.trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    @Override
    public int getOrder() {
        return -40; // Runs after JWT authentication (-50)
    }
}

