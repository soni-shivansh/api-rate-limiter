package com.shivansh.ratelimiterservice.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class InMemoryRateLimiterService {

    // Maximum number of requests allowed per minute for a single user.
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    // The time window in seconds (1 minute).
    private static final long TIME_WINDOW_IN_SECONDS = 60;

    // A thread-safe map to store request timestamps for each user (identified by IP address).
    // Key: User's IP address (String)
    // Value: A list of timestamps (Long) of their requests in the current window.
    private final ConcurrentMap<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    /**
     * Checks if a request from a given user (IP address) is allowed.
     * This method implements the "Sliding Window Log" algorithm.
     *
     * @param clientIp The IP address of the client making the request.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean isAllowed(String clientIp) {
        // Get the current time as a Unix timestamp (seconds since epoch).
        long currentTime = Instant.now().getEpochSecond();

        // Get the list of timestamps for the current user. If the user is new, create a new empty list.
        List<Long> timestamps = requestCounts.computeIfAbsent(clientIp, k -> new ArrayList<>());

        // Remove timestamps that are older than our time window (e.g., older than 60 seconds).
        // This is the "sliding" part of the window.
        timestamps.removeIf(ts -> ts < currentTime - TIME_WINDOW_IN_SECONDS);

        // Check if the number of requests in the current window is less than our limit.
        if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
            // If it is, add the current request's timestamp to the list and allow the request.
            timestamps.add(currentTime);
            return true;
        }

        // If the user has reached the limit, deny the request.
        return false;
    }
}