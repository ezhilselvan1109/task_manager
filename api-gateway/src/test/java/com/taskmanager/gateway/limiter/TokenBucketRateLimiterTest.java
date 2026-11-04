package com.taskmanager.gateway.limiter;

import com.taskmanager.gateway.config.RateLimiterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketRateLimiterTest {

    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        RateLimiterProperties properties = new RateLimiterProperties();
        properties.getAuth().setReplenishRate(5);
        properties.getAuth().setBurstCapacity(10);

        properties.getApi().setReplenishRate(60);
        properties.getApi().setBurstCapacity(20);

        properties.getActuator().setReplenishRate(120);
        properties.getActuator().setBurstCapacity(30);

        rateLimiter = new TokenBucketRateLimiter(properties, Clock.systemUTC());
    }

    @Test
    @DisplayName("Should enforce rate limit for auth tier per client IP")
    void testAuthTierRateLimiting() {
        String clientIp = "192.168.1.100";

        // Consume all 10 burst tokens
        for (int i = 0; i < 10; i++) {
            RateLimitResult result = rateLimiter.tryAcquire("auth", clientIp);
            assertThat(result.allowed()).isTrue();
            assertThat(result.remainingTokens()).isEqualTo(9 - i);
            assertThat(result.limit()).isEqualTo(10);
        }

        // 11th request should be rejected
        RateLimitResult rejected = rateLimiter.tryAcquire("auth", clientIp);
        assertThat(rejected.allowed()).isFalse();
        assertThat(rejected.remainingTokens()).isEqualTo(0);
        assertThat(rejected.retryAfterSeconds()).isGreaterThan(0);

        // Different IP should still have its own quota
        RateLimitResult otherIp = rateLimiter.tryAcquire("auth", "192.168.1.101");
        assertThat(otherIp.allowed()).isTrue();
        assertThat(otherIp.remainingTokens()).isEqualTo(9);

        // Null key fallback to "unknown"
        RateLimitResult nullKey = rateLimiter.tryAcquire("auth", null);
        assertThat(nullKey.allowed()).isTrue();
    }

    @Test
    @DisplayName("Should enforce rate limit for API tier per user ID")
    void testApiTierRateLimiting() {
        String userId = "00000000-0000-0000-0000-000000000001";

        // API tier burst capacity is 20
        for (int i = 0; i < 20; i++) {
            RateLimitResult result = rateLimiter.tryAcquire("api", userId);
            assertThat(result.allowed()).isTrue();
        }

        // 21st request should be rejected
        RateLimitResult rejected = rateLimiter.tryAcquire("api", userId);
        assertThat(rejected.allowed()).isFalse();
    }

    @Test
    @DisplayName("Should enforce rate limit for actuator tier")
    void testActuatorTierRateLimiting() {
        String clientIp = "127.0.0.1";
        RateLimitResult result = rateLimiter.tryAcquire("actuator", clientIp);
        assertThat(result.allowed()).isTrue();
        assertThat(result.limit()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should fallback gracefully when unknown tier is requested")
    void testUnknownTierFallback() {
        RateLimitResult result = rateLimiter.tryAcquire("unknown", "any-key");
        assertThat(result.allowed()).isTrue();
    }
}
