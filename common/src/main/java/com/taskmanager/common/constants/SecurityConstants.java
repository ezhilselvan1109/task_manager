package com.taskmanager.common.constants;

/**
 * Constants governing HTTP header contracts, token prefixes, and security roles.
 */
public final class SecurityConstants {

    /**
     * Injected internal header carrying the authenticated subject's UUID from API Gateway.
     */
    public static final String HEADER_USER_ID = "X-User-Id";

    /**
     * Injected internal header carrying the authenticated user's role from API Gateway.
     */
    public static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * Injected internal header carrying the authenticated user's email/username.
     */
    public static final String HEADER_USER_NAME = "X-User-Name";

    /**
     * Standard HTTP Authorization header name.
     */
    public static final String AUTH_HEADER = "Authorization";

    /**
     * Standard Bearer token prefix.
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Default user role authority.
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Administrator role authority.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}

