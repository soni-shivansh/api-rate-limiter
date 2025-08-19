package com.shivansh.ratelimiterservice.controller;

import com.shivansh.ratelimiterservice.service.InMemoryRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimiterController {

    private final InMemoryRateLimiterService rateLimiterService;

    public RateLimiterController(InMemoryRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/v0/limited")
    public ResponseEntity<String> getLimitedResource(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        if (rateLimiterService.isAllowed(clientIp)) {
            return ResponseEntity.ok("Success! You can access the resource.");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too Many Requests! Please try again later.");
        }
    }

}
