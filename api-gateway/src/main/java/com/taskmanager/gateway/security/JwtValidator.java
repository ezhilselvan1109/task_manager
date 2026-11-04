package com.taskmanager.gateway.security;

import com.taskmanager.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * Validates HMAC-SHA256 signed JWT access tokens at the API Gateway perimeter.
 * Guarantees zero sensitive logging and strict claim verification.
 */
@Component
public class JwtValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtValidator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = resolveSigningKey(jwtProperties.getSecret());
    }

    private SecretKey resolveSigningKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates an access token and extracts authenticated identity claims.
     *
     * @param token Compact serialized JWT string.
     * @return JwtValidationResult containing either validated UserIdentity or failure code.
     */
    public JwtValidationResult validate(String token) {
        if (token == null || token.isBlank()) {
            return JwtValidationResult.failure("MISSING_TOKEN", "JWT access token is missing or empty");
        }

        Claims claims;
        try {
            claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token has expired");
            return JwtValidationResult.failure("EXPIRED_TOKEN", "JWT access token has expired");
        } catch (SecurityException e) {
            log.warn("JWT signature verification failed");
            return JwtValidationResult.failure("INVALID_SIGNATURE", "JWT cryptographic signature is invalid");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token presented");
            return JwtValidationResult.failure("MALFORMED_TOKEN", "Malformed or unsupported JWT access token");
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getClass().getSimpleName());
            return JwtValidationResult.failure("INVALID_TOKEN", "JWT token validation failed");
        }

        // Validate token issuer
        if (jwtProperties.getIssuer() != null && !jwtProperties.getIssuer().equals(claims.getIssuer())) {
            log.warn("JWT issuer mismatch: expected '{}', got '{}'", jwtProperties.getIssuer(), claims.getIssuer());
            return JwtValidationResult.failure("INVALID_ISSUER", "Token issuer does not match trusted gateway authority");
        }

        // Validate token type (reject refresh tokens)
        String tokenType = claims.get("type", String.class);
        if ("refresh".equalsIgnoreCase(tokenType)) {
            log.warn("Refresh token presented on protected resource route");
            return JwtValidationResult.failure("INVALID_TOKEN_TYPE", "Refresh token cannot be used as an access token");
        }

        // Validate required claim: userId
        String userIdStr = claims.get("userId", String.class);
        if (userIdStr == null || userIdStr.isBlank()) {
            return JwtValidationResult.failure("MISSING_CLAIMS", "Missing required claim: userId");
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            return JwtValidationResult.failure("INVALID_USER_ID", "Claim userId is not a valid UUID format");
        }

        // Validate required claim: role
        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            return JwtValidationResult.failure("MISSING_CLAIMS", "Missing required claim: role");
        }

        // Extract email / username
        String email = claims.get("email", String.class);
        if (email == null || email.isBlank()) {
            email = claims.getSubject();
        }

        return JwtValidationResult.success(new UserIdentity(userId, role, email));
    }
}

