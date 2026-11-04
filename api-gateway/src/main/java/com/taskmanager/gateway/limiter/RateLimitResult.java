package com.taskmanager.gateway.limiter;

/**
 * Result model representing the outcome of a rate-limiting token consumption attempt.
 *
 * @param allowed Whether the request was permitted to proceed.
 * @param remainingTokens Tokens remaining in the token bucket after this operation.
 * @param limit The burst capacity configured for this route tier.
 * @param retryAfterSeconds Recommended client wait duration in seconds before retrying upon 429 rejection.
 */
public record RateLimitResult(
        boolean allowed,
        int remainingTokens,
        int limit,
        long retryAfterSeconds
) {
}

