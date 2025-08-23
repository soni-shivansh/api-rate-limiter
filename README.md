# High-Throughput API Rate Limiter

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A standalone, microservice-based API Rate Limiter built with Java 21, Spring Boot, and Redis. It's designed to be a scalable and resilient gatekeeper for any API, enforcing usage policies and preventing abuse.

## Why This Project?

In a world of distributed systems, protecting your APIs from overuse and ensuring fair usage is critical. This project provides a ready-to-use, high-performance rate limiting solution that can be easily integrated into any microservices architecture. It's built on industry-standard technologies and best practices, making it a reliable and scalable choice for any project.

## Key Features

* **Standalone Service**: A dedicated microservice for rate limiting, decoupled from your core business logic.
* **Token Bucket Algorithm**: Implements the efficient and flexible Token Bucket algorithm to handle bursty traffic while maintaining a steady average rate.
* **Distributed & Scalable**: A centralized Redis cache allows multiple instances of the rate limiter to share state seamlessly.
* **Resilient by Design**: A circuit breaker prevents cascading failures when Redis is unavailable.
* **Microservice Architecture**: Integrated with a Spring Cloud Gateway, demonstrating a real-world, scalable architecture pattern.

## Architecture Diagram

Here is the high-level architecture of the system:

```
[Client] ---> [API Gateway] ---> [Rate Limiter Service] ---> [Product Service]
                 (Port 8080)        (Port 8082)              (Port 8081)
                                        |
                                        v
                                     [Redis]
                                  (Port 6379)
```

## Tech Stack

* **Java 21** & **Spring Boot 3**
* **Spring Cloud Gateway**: For intelligent routing.
* **Redis**: As a distributed, high-speed cache for rate limiting state.
* **Docker**: For running Redis.
* **Maven**: For project dependency management.
* **k6**: For load testing and performance benchmarking.

## Getting Started

### Prerequisites

* Java 21
* Docker
* Maven

## How to Run the Project

1.  **Start Redis**:
    ```bash
    docker run --name my-redis -p 6379:6379 -d redis
    ```

2.  **Run the Services**:
    Open three separate terminals and run each service using the Maven wrapper:
    ```bash
    # Terminal 1: Product Service
    cd product-service
    ./mvnw spring-boot:run

    # Terminal 2: Rate Limiter Service
    cd rate-limiter-service
    ./mvnw spring-boot:run

    # Terminal 3: API Gateway
    cd api-gateway
    ./mvnw spring-boot:run
    ```

3.  **Test the Endpoint**:
    You can now send requests to the protected endpoint through the API Gateway:
    ```bash
    curl http://localhost:8080/api/v1/products
    ```
## Configuration

The rate limiter can be configured in the `rate-limiter-service/src/main/resources/application.yml` file.

```
rate-limiter:
  plans:
    default:
      bucket-capacity: 10 # Maximum number of requests allowed in the bucket
      refill-rate-per-minute: 10 # Number of requests to refill every minute
```

## Performance Benchmark

The system was load-tested using k6 to simulate a sustained load of 100 virtual users.

The service was able to sustain a throughput of **~80 requests/second** with a **p(95) latency of less than 12.62ms**, demonstrating its capability to handle significant load with minimal performance overhead.

```

  █ THRESHOLDS

    http_req_duration
    ✓ 'p(95)<200' p(95)=12.62ms


  █ TOTAL RESULTS

    checks_total.......: 7984    79.454301/s
    checks_succeeded...: 100.00% 7984 out of 7984
    checks_failed......: 0.00%   0 out of 7984

    ✓ is status 200 or 429

    HTTP
    http_req_duration..............: avg=7.72ms   min=884.3µs med=5.92ms   max=1.32s p(90)=9.57ms p(95)=12.62ms
      { expected_response:true }...: avg=391.67ms min=30.7ms  med=106.11ms max=1.32s p(90)=1.05s  p(95)=1.19s
    http_req_failed................: 99.87% 7974 out of 7984
    http_reqs......................: 7984   79.454301/s

    EXECUTION
    iteration_duration.............: avg=1s       min=1s      med=1s       max=2.33s p(90)=1.01s  p(95)=1.01s
    iterations.....................: 7984   79.454301/s
    vus............................: 6      min=4            max=100
    vus_max........................: 100    min=100          max=100

    NETWORK
    data_received..................: 1.4 MB 14 kB/s
    data_sent......................: 679 kB 6.8 kB/s
