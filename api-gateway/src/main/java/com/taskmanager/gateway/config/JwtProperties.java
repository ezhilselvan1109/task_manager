package com.taskmanager.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties binding for JWT perimeter token validation.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Base64 encoded or raw HMAC-SHA256 secret key (minimum 256 bits).
     */
    private String secret = "dGFza21hbmFnZXItc3VwZXItc2VjcmV0LWtleS0yNTYtYml0cy1taW5pbXVtLWZvci1oczI1Ng==";

    /**
     * Expected token issuer claim.
     */
    private String issuer = "task-manager-auth";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}

