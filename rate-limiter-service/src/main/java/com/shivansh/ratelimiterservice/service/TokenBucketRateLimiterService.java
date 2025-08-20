package com.shivansh.ratelimiterservice.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBucketRateLimiterService {

    // The maximum number of tokens the bucket can hold (burst capacity).
    private static final int BUCKET_CAPACITY = 10;
    // The number of tokens to add to the bucket per minute.
    private static final int REFILL_RATE_PER_MINUTE = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public TokenBucketRateLimiterService(RedisTemplate<String, String> redisTemplate, DefaultRedisScript<Long> redisScript) {
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

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

}
