package com.shivansh.ratelimiterservice.controller;

import com.shivansh.ratelimiterservice.service.TokenBucketRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimiterController {

    private final TokenBucketRateLimiterService rateLimiterService;

    public RateLimiterController(TokenBucketRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/v1/limited")
    public ResponseEntity<String> getLimitedResource(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        if (rateLimiterService.isAllowed(clientIp)) {
            return ResponseEntity.ok("Success! You can access the resource (v1 - Redis).");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too Many Requests! Please try again later (v1 - Redis).");
        }
    }

}
