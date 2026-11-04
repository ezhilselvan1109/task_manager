package com.taskmanager.gateway.security;

import java.util.UUID;

/**
 * Validated security identity extracted from JWT claims at the API Gateway perimeter.
 *
 * @param userId Unique subject UUID.
 * @param role Authenticated security role (e.g. ROLE_USER, ROLE_ADMIN).
 * @param email Authenticated user email address.
 */
public record UserIdentity(
        UUID userId,
        String role,
        String email
) {
}

