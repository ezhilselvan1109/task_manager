package com.taskmanager.gateway.limiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    @DisplayName("Should initialize with burst capacity and consume tokens successfully")
    void testInitialCapacityAndConsumption() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-04T00:00:00Z"), ZoneId.of("UTC"));
        TokenBucket bucket = new TokenBucket(10, 5, fixedClock); // burst: 10, replenish: 5 tokens/min

        assertThat(bucket.getAvailableTokens()).isEqualTo(10);
        assertThat(bucket.tryConsume(1)).isTrue();
        assertThat(bucket.getAvailableTokens()).isEqualTo(9);

        assertThat(bucket.tryConsume(9)).isTrue();
        assertThat(bucket.getAvailableTokens()).isEqualTo(0);

        // Cannot consume when empty
        assertThat(bucket.tryConsume(1)).isFalse();
        assertThat(bucket.getAvailableTokens()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should replenish tokens over time based on replenish rate without exceeding burst capacity")
    void testRefillOverTime() {
        Instant startTime = Instant.parse("2026-11-04T00:00:00Z");
        MutableTestClock mutableClock = new MutableTestClock(startTime, ZoneId.of("UTC"));
        TokenBucket bucket = new TokenBucket(10, 60, mutableClock); // replenish: 60 tokens/min (1 token/sec), burst: 10

        // Consume all 10 tokens
        assertThat(bucket.tryConsume(10)).isTrue();
        assertThat(bucket.getAvailableTokens()).isEqualTo(0);
        assertThat(bucket.tryConsume(1)).isFalse();

        // Advance 3 seconds -> should add 3 tokens
        mutableClock.advanceSeconds(3);
        assertThat(bucket.getAvailableTokens()).isEqualTo(3);
        assertThat(bucket.tryConsume(2)).isTrue();
        assertThat(bucket.getAvailableTokens()).isEqualTo(1);

        // Advance 100 seconds -> should replenish up to burst capacity (10), not beyond
        mutableClock.advanceSeconds(100);
        assertThat(bucket.getAvailableTokens()).isEqualTo(10);
    }

    private static class MutableTestClock extends Clock {
        private Instant currentInstant;
        private final ZoneId zone;

        public MutableTestClock(Instant initialInstant, ZoneId zone) {
            this.currentInstant = initialInstant;
            this.zone = zone;
        }

        public void advanceSeconds(long seconds) {
            this.currentInstant = currentInstant.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableTestClock(currentInstant, zone);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}

