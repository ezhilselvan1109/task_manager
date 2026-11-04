package com.taskmanager.gateway.limiter;

import java.time.Clock;

/**
 * Thread-safe in-memory Token Bucket implementation conforming to DOC-RES-005.
 * Tracks capacity, consumption, and continuous token replenishment.
 */
public class TokenBucket {

    private final int burstCapacity;
    private final int replenishRatePerMinute;
    private final Clock clock;

    private int availableTokens;
    private long lastRefillTimestampMs;

    public TokenBucket(int burstCapacity, int replenishRatePerMinute, Clock clock) {
        this.burstCapacity = burstCapacity;
        this.replenishRatePerMinute = replenishRatePerMinute;
        this.clock = clock;
        this.availableTokens = burstCapacity;
        this.lastRefillTimestampMs = clock.millis();
    }

    /**
     * Attempts to consume the specified number of tokens from the bucket.
     *
     * @param tokens Number of tokens to consume.
     * @return true if tokens were successfully consumed, false if insufficient tokens remain.
     */
    public synchronized boolean tryConsume(int tokens) {
        refill();
        if (availableTokens >= tokens) {
            availableTokens -= tokens;
            return true;
        }
        return false;
    }

    /**
     * Returns the currently available tokens after accounting for time elapsed.
     */
    public synchronized int getAvailableTokens() {
        refill();
        return availableTokens;
    }

    private void refill() {
        long now = clock.millis();
        long elapsedMs = now - lastRefillTimestampMs;

        if (elapsedMs <= 0) {
            return;
        }

        // Calculate tokens to add based on elapsed time and per-minute rate
        double tokensToAdd = (elapsedMs * replenishRatePerMinute) / 60000.0;
        if (tokensToAdd >= 1.0) {
            int wholeTokens = (int) tokensToAdd;
            availableTokens = Math.min(burstCapacity, availableTokens + wholeTokens);
            // Advance lastRefillTimestamp by the portion consumed to prevent sub-token loss
            long timeConsumedMs = (long) ((wholeTokens * 60000.0) / replenishRatePerMinute);
            lastRefillTimestampMs += timeConsumedMs;
        }
    }
}

