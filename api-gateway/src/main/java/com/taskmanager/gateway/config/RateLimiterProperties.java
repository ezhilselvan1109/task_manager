package com.taskmanager.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for token-bucket rate limiting tiers conforming to DOC-RES-005.
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private TierConfig auth = new TierConfig(5, 10);
    private TierConfig api = new TierConfig(60, 20);
    private TierConfig actuator = new TierConfig(120, 30);

    public TierConfig getAuth() {
        return auth;
    }

    public void setAuth(TierConfig auth) {
        this.auth = auth;
    }

    public TierConfig getApi() {
        return api;
    }

    public void setApi(TierConfig api) {
        this.api = api;
    }

    public TierConfig getActuator() {
        return actuator;
    }

    public void setActuator(TierConfig actuator) {
        this.actuator = actuator;
    }

    public static class TierConfig {
        private int replenishRate;
        private int burstCapacity;

        public TierConfig() {
        }

        public TierConfig(int replenishRate, int burstCapacity) {
            this.replenishRate = replenishRate;
            this.burstCapacity = burstCapacity;
        }

        public int getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }
}

