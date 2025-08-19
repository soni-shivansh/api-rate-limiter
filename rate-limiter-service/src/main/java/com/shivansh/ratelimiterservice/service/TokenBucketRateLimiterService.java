package com.shivansh.ratelimiterservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBucketRateLimiterService {

    // The maximum number of tokens the bucket can hold (burst capacity).
    private static final int BUCKET_CAPACITY = 10;
    // The number of tokens to add to the bucket per minute.
    private static final int REFILL_RATE_PER_MINUTE = 10;

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBucketRateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientIp) {
        String tokensKey = "rate_limiter:tokens:" + clientIp;
        String timestampKey = "rate_limiter:timestamp:" + clientIp;

        String currentTokensStr = redisTemplate.opsForValue().get(tokensKey);
        String lastRefillTimestampStr = redisTemplate.opsForValue().get(timestampKey);

        long currentTokens;
        long lastRefillTimestamp;

        if (currentTokensStr == null || lastRefillTimestampStr == null) {
            // If the user is new, initialize their bucket with full capacity.
            currentTokens = BUCKET_CAPACITY;
            lastRefillTimestamp = Instant.now().getEpochSecond();
        } else {
            currentTokens = Long.parseLong(currentTokensStr);
            lastRefillTimestamp = Long.parseLong(lastRefillTimestampStr);
        }

        long now = Instant.now().getEpochSecond();
        long elapsedTime = now - lastRefillTimestamp;

        // Refill the bucket with new tokens based on elapsed time.
        if (elapsedTime > 0) {
            long tokensToAdd = (elapsedTime * REFILL_RATE_PER_MINUTE) / 60;
            currentTokens = Math.min(currentTokens + tokensToAdd, BUCKET_CAPACITY);
            lastRefillTimestamp = now;
        }

        // Check if the user has at least one token to spend.
        if (currentTokens >= 1) {
            // Consume one token.
            currentTokens--;

            // Save the new state back to Redis with a 2-minute expiration.
            redisTemplate.opsForValue().set(tokensKey, String.valueOf(currentTokens), 2, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(timestampKey, String.valueOf(lastRefillTimestamp), 2, TimeUnit.MINUTES);
            return true;
        } else {
            // Save the current state without consuming a token.
            redisTemplate.opsForValue().set(tokensKey, String.valueOf(currentTokens), 2, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(timestampKey, String.valueOf(lastRefillTimestamp), 2, TimeUnit.MINUTES);
            return false;
        }
    }

}
