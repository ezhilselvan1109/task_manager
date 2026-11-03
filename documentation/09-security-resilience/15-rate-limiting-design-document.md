# Rate Limiting Design Document

**Document Identifier:** DOC-RES-005  
**Traceability References:** SEC-009, NFR-002, NFR-003, ADR-007  
**Target Services:** `api-gateway`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the rate limiting architecture enforced at the API Gateway. Rate limiting protects the platform from denial-of-service (DoS) attacks, brute-force credential stuffing, runaway client scripts, and resource starvation across microservices.

---

## 2. Rate Limiting Algorithm: Token Bucket

The system utilizes the **Token Bucket Algorithm**:
- A bucket holds a maximum number of tokens (Burst Capacity).
- Tokens refill at a constant rate per second/minute (Replenish Rate).
- Each inbound request consumes 1 token.
- If tokens are available: Request is permitted.
- If the bucket is empty: Request is immediately rejected with `HTTP 429 Too Many Requests`.

```
[ Inbound Request ] ───▶ [ Check Bucket ]
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
        [ Tokens >= 1 ]               [ Tokens == 0 ]
               │                             │
    Decrement Token by 1                     │
               │                             ▼
               ▼                     Reject Request:
     Forward to Microservice      HTTP 429 Too Many Requests
                                  Retry-After: N seconds
```

---

## 3. Rate Limit Tiers and Policy Matrix

| Tier Name | Target Route | Partitioning Key | Replenish Rate | Burst Capacity | Protective Objective |
|---|---|---|---|---|---|
| **Auth Tier** | `/api/v1/auth/**` | Client Remote IP | 5 requests / min | 10 tokens | Prevents automated credential stuffing and BCrypt CPU exhaustion. |
| **User API Tier** | `/api/v1/tasks/**`<br>`/api/v1/notifications/**` | Authenticated `userId` | 60 requests / min | 20 tokens | Prevents rogue client scripts from monopolizing task database IOPS. |
| **Public Actuator**| `/actuator/health` | Client Remote IP | 120 requests / min| 30 tokens | Allows frequent load-balancer health checks while preventing flood attacks. |

---

## 4. Spring Cloud Gateway In-Memory Token Bucket Filter

In alignment with `ADR-007` (zero-Redis lightweight local footprint), the gateway implements an in-memory thread-safe `ConcurrentHashMap` with Google Guava / Bucket4j semantics:

```java
@Component
public class TokenBucketRateLimiterGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<TokenBucketRateLimiterGatewayFilterFactory.Config> {

    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String routeKey = resolveKey(exchange);
            Bucket bucket = bucketCache.computeIfAbsent(routeKey, k -> createNewBucket(config));

            if (bucket.tryConsume(1)) {
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", 
                        String.valueOf(bucket.getAvailableTokens()));
                return chain.filter(exchange);
            } else {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                response.getHeaders().add("Retry-After", "60");
                
                byte[] bytes = formatProblemDetailJson();
                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            }
        };
    }
}
```

---

## 5. Rate Limit Exceeded HTTP 429 Response

When rate limits are exceeded, the API Gateway immediately short-circuits the request and returns an RFC 7807 payload without burdening downstream microservices:

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/problem+json
Retry-After: 60
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 0

{
  "type": "https://api.taskmanager.com/errors/rate-limit-exceeded",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Rate limit exceeded. Maximum 60 requests per minute permitted.",
  "instance": "/api/v1/tasks",
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "timestamp": "2026-11-03T18:00:00Z"
}
```

