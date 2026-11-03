---
name: spring-cloud-api-gateway
description: >-
  Use this skill when developing, configuring, or troubleshooting Spring Cloud Gateway routes,
  reactive filters, rate limiting, and Eureka service discovery integration.
---

# Spring Cloud & API Gateway Skill

## Purpose
Guides the configuration and implementation of the reactive Spring Cloud Gateway edge proxy, Eureka discovery registration, JWT authentication filters, correlation ID injection, and token bucket rate limiters.

## When to Use
- Adding or modifying API route definitions in `api-gateway`.
- Implementing custom reactive `GlobalFilter` or `GatewayFilterFactory` components.
- Configuring Eureka client registration or load balancer policies (`lb://`).
- Setting up rate limiting on public or sensitive routes.

## Required Inputs
- Gateway configuration file `api-gateway/src/main/resources/application.yml`.
- Target microservice route definitions and path patterns.

## Step-by-Step Procedure
1. **Configure Eureka Client Integration**:
   Ensure gateway registers with Eureka:
   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
   ```
2. **Define Reactive Route Predicates**:
   Route incoming paths to target services using load-balanced URIs:
   ```yaml
   spring:
     cloud:
       gateway:
         routes:
           - id: user-service-route
             uri: lb://user-service
             predicates:
               - Path=/api/v1/auth/**, /api/v1/users/**
           - id: task-service-route
             uri: lb://task-service
             predicates:
               - Path=/api/v1/tasks/**
           - id: notification-service-route
             uri: lb://notification-service
             predicates:
               - Path=/api/v1/notifications/**
   ```
3. **Implement Reactive Authentication Filter**:
   Create `JwtAuthenticationFilter` implementing `GlobalFilter` (or `AbstractGatewayFilterFactory`):
   - Check whitelist paths (e.g., `/api/v1/auth/register`, `/api/v1/auth/login`).
   - Extract `Authorization: Bearer <token>`.
   - Validate HMAC-SHA256 signature and expiration.
   - Mutate request headers with `X-User-Id` and `X-User-Role`.
4. **Implement Correlation ID Filter**:
   - Extract `X-Correlation-ID` or generate new UUID via `UUID.randomUUID().toString()`.
   - Forward to downstream and append to outgoing `ServerHttpResponse` headers.
5. **Configure Redis/In-Memory Rate Limiting**:
   - Attach KeyResolver (by client IP or user ID) and RequestRateLimiter filter to auth routes.

## Applicable Project Standards
- [Rule 03: Spring Cloud & API Gateway Standards](file:///d:/learning/task_manager/.agents/rules/03-spring-cloud-and-gateway.md)
- [API Gateway Routing Document](file:///d:/learning/task_manager/documentation/07-api-design/14-api-gateway-routing-document.md)

## Validation Checklist
- [ ] Route paths correctly forward to `lb://<service-name>`.
- [ ] Correlation ID is present in both downstream request headers and client response headers.
- [ ] Invalid or expired tokens return `401 Unauthorized` without calling downstream services.
- [ ] Whitelisted endpoints (`/api/v1/auth/login`) are accessible without tokens.

## Common Mistakes
- Using blocking Servlet APIs inside Spring Cloud Gateway (it uses reactive Project Reactor / Netty).
- Forgetting to mutate request when adding headers (must use `request.mutate().header(...).build()`).

## Completion Criteria
Gateway successfully routes incoming requests to registered Eureka services and correctly enforces JWT validation and correlation ID injection.

