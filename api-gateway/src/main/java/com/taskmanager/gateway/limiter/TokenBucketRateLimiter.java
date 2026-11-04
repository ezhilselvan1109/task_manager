package com.taskmanager.gateway.limiter;

import com.taskmanager.gateway.config.RateLimiterProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages partition-specific TokenBucket instances per route tier and partition key.
 * Guarantees zero-Redis in-memory token bucket enforcement according to DOC-RES-005.
 */
@Component
public class TokenBucketRateLimiter {

    private final RateLimiterProperties properties;
    private final Clock clock;
    private final ConcurrentMap<String, TokenBucket> buckets;

    public TokenBucketRateLimiter(RateLimiterProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.buckets = new ConcurrentHashMap<>();
    }

    /**
     * Attempts to acquire 1 token for the specified tier and key.
     *
     * @param tier Route rate limiting tier (e.g. "auth", "api", "actuator").
     * @param key Partition key (e.g. client IP or authenticated userId).
     * @return RateLimitResult indicating success or rejection with retry headers.
     */
    public RateLimitResult tryAcquire(String tier, String key) {
        RateLimiterProperties.TierConfig tierConfig = resolveTierConfig(tier);
        if (tierConfig == null) {
            return new RateLimitResult(true, 100, 100, 0);
        }

        String compositeKey = tier + ":" + (key != null ? key : "unknown");
        TokenBucket bucket = buckets.computeIfAbsent(
                compositeKey,
                k -> new TokenBucket(tierConfig.getBurstCapacity(), tierConfig.getReplenishRate(), clock)
        );

        boolean allowed = bucket.tryConsume(1);
        int remaining = bucket.getAvailableTokens();
        long retryAfter = allowed ? 0 : 60;

        return new RateLimitResult(allowed, remaining, tierConfig.getBurstCapacity(), retryAfter);
    }

    private RateLimiterProperties.TierConfig resolveTierConfig(String tier) {
        if ("auth".equalsIgnoreCase(tier)) {
            return properties.getAuth();
        } else if ("api".equalsIgnoreCase(tier)) {
            return properties.getApi();
        } else if ("actuator".equalsIgnoreCase(tier)) {
            return properties.getActuator();
        }
        return null;
    }
}

