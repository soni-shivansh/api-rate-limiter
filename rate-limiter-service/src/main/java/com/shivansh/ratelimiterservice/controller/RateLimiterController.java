package com.shivansh.ratelimiterservice.controller;

import com.shivansh.ratelimiterservice.service.TokenBucketRateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class RateLimiterController {

    private final TokenBucketRateLimiterService rateLimiterService;
    private final WebClient.Builder webClientBuilder;

    public RateLimiterController(TokenBucketRateLimiterService rateLimiterService, WebClient.Builder webClientBuilder) {
        this.rateLimiterService = rateLimiterService;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/v1/products")
    public Mono<ResponseEntity<String>> getLimitedResource(ServerWebExchange exchange) {
        String clientIp = "127.0.0.1";

        if (exchange.getRequest().getRemoteAddress() != null) {
            clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }


        if (rateLimiterService.isAllowed(clientIp)) {
            return webClientBuilder.build()
                    .get()
                    .uri("http://product-service:8081/products") // The actual protected service
                    .retrieve()
                    .toEntity(String.class)
                    .onErrorResume(WebClientResponseException.class,
                            ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString())));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too Many Requests! Please try again later."));
        }
    }

}
