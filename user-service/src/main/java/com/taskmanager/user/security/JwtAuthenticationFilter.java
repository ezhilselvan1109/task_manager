package com.taskmanager.user.security;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filter that intercepts inbound HTTP requests to establish authentication and MDC tracing context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Propagate or generate correlation ID into MDC
        String correlationId = request.getHeader(CorrelationConstants.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CorrelationConstants.MDC_KEY, correlationId);
        response.setHeader(CorrelationConstants.HEADER_NAME, correlationId);

        try {
            String authHeader = request.getHeader(SecurityConstants.AUTH_HEADER);
            String token = null;

            if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
                token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.extractUserId(token);
                String email = jwtTokenProvider.extractEmail(token);
                String role = jwtTokenProvider.extractRole(token);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(role != null ? role : SecurityConstants.ROLE_USER)
                    );

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    MDC.put("userId", userId.toString());
                }
            } else {
                // Support header-based identity propagation from trusted API Gateway
                String gatewayUserId = request.getHeader(SecurityConstants.HEADER_USER_ID);
                String gatewayRole = request.getHeader(SecurityConstants.HEADER_USER_ROLE);

                if (gatewayUserId != null && !gatewayUserId.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        UUID userId = UUID.fromString(gatewayUserId);
                        String role = (gatewayRole != null && !gatewayRole.isBlank()) ? gatewayRole : SecurityConstants.ROLE_USER;

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        MDC.put("userId", userId.toString());
                    } catch (IllegalArgumentException e) {
                        log.warn("Malformed X-User-Id header received: {}", gatewayUserId);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationConstants.MDC_KEY);
            MDC.remove("userId");
        }
    }
}

