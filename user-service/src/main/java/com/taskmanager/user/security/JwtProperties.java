package com.taskmanager.user.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JSON Web Token operations.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Base64-encoded or raw 256-bit secret key for HMAC-SHA256 signing.
     */
    private String secret = "dGFza21hbmFnZXItc3VwZXItc2VjcmV0LWtleS0yNTYtYml0cy1taW5pbXVtLWZvci1oczI1Ng==";

    /**
     * Token issuer claim value (iss).
     */
    private String issuer = "task-manager-auth";

    /**
     * Access token validity duration in seconds (default 900s = 15m).
     */
    private long accessTokenExpirationSeconds = 900L;

    /**
     * Refresh token validity duration in days (default 7 days).
     */
    private long refreshTokenExpirationDays = 7L;
}

