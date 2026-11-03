# API Gateway Implementation Guide

**Document Identifier:** DOC-DEV-003  
**Traceability References:** ADR-003, ADR-005, DOC-API-014, DOC-SEC-002  
**Target Module:** `api-gateway`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the implementation guide for the `api-gateway` microservice. Built upon Spring Cloud Gateway (Netty-based reactive WebFlux), the Gateway serves as the single perimeter ingress, managing dynamic Eureka routing, JWT authentication, header sanitization, correlation ID propagation, and circuit breaking.

---

## 2. Dependencies (`api-gateway/pom.xml`)

```xml
<dependencies>
    <!-- Spring Cloud Gateway Reactive -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>

    <!-- Eureka Service Discovery Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <!-- Reactive Resilience4j Circuit Breaker -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
    </dependency>

    <!-- JJWT for Stateless Token Verification -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jjwt.version}</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>${jjwt.version}</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>${jjwt.version}</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Observability -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Dynamic Route Configuration (`application.yml`)

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false             # Explicit routes enforced for strict control
      routes:
        # Route 1: Authentication Endpoints (Public)
        - id: user-auth-route
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - HeaderSanitizationFilter

        # Route 2: User Profile Endpoints (Protected)
        - id: user-profile-route
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/v1/users/**
          filters:
            - HeaderSanitizationFilter
            - JwtAuthenticationFilter

        # Route 3: Task Service Endpoints (Protected with Circuit Breaker)
        - id: task-service-route
          uri: lb://TASK-SERVICE
          predicates:
            - Path=/api/v1/tasks/**
          filters:
            - HeaderSanitizationFilter
            - JwtAuthenticationFilter
            - name: CircuitBreaker
              args:
                name: taskServiceCircuitBreaker
                fallbackUri: forward:/fallback/tasks

        # Route 4: Notification Service Endpoints (Protected)
        - id: notification-service-route
          uri: lb://NOTIFICATION-SERVICE
          predicates:
            - Path=/api/v1/notifications/**
          filters:
            - HeaderSanitizationFilter
            - JwtAuthenticationFilter
```

---

## 4. Key Filter Implementations

### 4.1 Header Sanitization Pre-Filter
Strips any inbound `X-User-Id` or `X-User-Role` headers sent by external clients to prevent spoofing:
```java
@Component
public class HeaderSanitizationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Role");
                    httpHeaders.remove("X-User-Name");
                })
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100; // Execute before authentication filters
    }
}
```

### 4.2 Correlation ID Injection Filter
Ensures every request carries an `X-Correlation-Id` header:
```java
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        exchange.getResponse().getHeaders().add("X-Correlation-Id", correlationId);
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -200; // First filter in chain
    }
}
```

