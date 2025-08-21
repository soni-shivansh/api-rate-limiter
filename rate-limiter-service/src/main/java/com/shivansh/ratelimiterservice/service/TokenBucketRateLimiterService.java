package com.shivansh.ratelimiterservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenBucketRateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBucketRateLimiterService.class);
    private static final String REDIS_CIRCUIT_BREAKER = "redis";

    private static final int BUCKET_CAPACITY = 10;
    private static final int REFILL_RATE_PER_MINUTE = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public TokenBucketRateLimiterService(RedisTemplate<String, String> redisTemplate, DefaultRedisScript<Long> redisScript) {
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    @CircuitBreaker(name = REDIS_CIRCUIT_BREAKER, fallbackMethod = "fallbackIsAllowed")
    public boolean isAllowed(String clientIp) {
        String tokensKey = "rate_limiter:tokens:" + clientIp;
        String timestampKey = "rate_limiter:timestamp:" + clientIp;

        Long result = redisTemplate.execute(
                redisScript,
                List.of(tokensKey, timestampKey),
                String.valueOf(REFILL_RATE_PER_MINUTE),
                String.valueOf(BUCKET_CAPACITY),
                String.valueOf(Instant.now().getEpochSecond())
        );
        return result == 1L;
    }

    public boolean fallbackIsAllowed(String clientIp, Throwable t) {
        logger.warn("Redis is down or unresponsive. Failing open for client IP: {}. Error: {}", clientIp, t.getMessage());
        return true;
    }

}
