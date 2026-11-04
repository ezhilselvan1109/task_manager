package com.taskmanager.gateway.filter;

import com.taskmanager.common.constants.SecurityConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Perimeter defense filter stripping client-supplied identity headers to prevent header spoofing attacks.
 * Guarantees downstream microservices receive identity headers derived strictly from verified JWT tokens.
 */
@Component
public class HeaderSanitizationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove(SecurityConstants.HEADER_USER_ID);
                    httpHeaders.remove(SecurityConstants.HEADER_USER_ROLE);
                    httpHeaders.remove(SecurityConstants.HEADER_USER_NAME);
                })
                .build();

        return chain.filter(exchange.mutate().request(sanitizedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100; // Executes after correlation ID filter, before authentication
    }
}

