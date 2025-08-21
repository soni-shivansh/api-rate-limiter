package com.shivansh.ratelimiterservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private Map<String, Plan> plans;

    public Map<String, Plan> getPlans() {
        return plans;
    }

    public void setPlans(Map<String, Plan> plans) {
        this.plans = plans;
    }

    public static class Plan {
        private int bucketCapacity;
        private int refillRatePerMinute;

        public int getBucketCapacity() {
            return bucketCapacity;
        }

        public void setBucketCapacity(int bucketCapacity) {
            this.bucketCapacity = bucketCapacity;
        }

        public int getRefillRatePerMinute() {
            return refillRatePerMinute;
        }

        public void setRefillRatePerMinute(int refillRatePerMinute) {
            this.refillRatePerMinute = refillRatePerMinute;
        }
    }
}