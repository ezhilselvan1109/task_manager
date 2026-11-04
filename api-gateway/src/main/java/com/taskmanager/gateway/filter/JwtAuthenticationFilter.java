package com.taskmanager.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.gateway.handler.GatewayErrorWriter;
import com.taskmanager.gateway.security.JwtValidationResult;
import com.taskmanager.gateway.security.JwtValidator;
import com.taskmanager.gateway.security.UserIdentity;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Perimeter authentication filter verifying stateless HMAC-SHA256 JWT access tokens.
 * Injects verified downstream identity headers: X-User-Id, X-User-Role, and X-User-Name.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATH_PATTERNS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/**",
            "/fallback/**"
    );

    private final JwtValidator jwtValidator;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtValidator jwtValidator, ObjectMapper objectMapper) {
        this.jwtValidator = jwtValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return GatewayErrorWriter.writeErrorResponse(
                    exchange,
                    objectMapper,
                    HttpStatus.UNAUTHORIZED,
                    "MISSING_AUTHORIZATION_HEADER",
                    "Authorization header with Bearer token is required"
            );
        }

        String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        JwtValidationResult result = jwtValidator.validate(token);

        if (!result.isValid()) {
            return GatewayErrorWriter.writeErrorResponse(
                    exchange,
                    objectMapper,
                    HttpStatus.UNAUTHORIZED,
                    result.getErrorCode(),
                    result.getErrorMessage()
            );
        }

        UserIdentity identity = result.getIdentity();
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(SecurityConstants.HEADER_USER_ID, identity.userId().toString())
                .header(SecurityConstants.HEADER_USER_ROLE, identity.role())
                .header(SecurityConstants.HEADER_USER_NAME, identity.email())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATH_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -50; // Runs after header sanitization (-100) and before rate limiting (-40)
    }
}

