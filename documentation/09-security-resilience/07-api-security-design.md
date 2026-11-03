# API Security Design

**Document Identifier:** DOC-SEC-007  
**Traceability References:** SEC-006, SEC-007, SEC-009, NFR-003, NFR-004  
**Target Services:** `api-gateway`, All Microservices  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies perimeter security defenses, HTTP header hardening, Cross-Origin Resource Sharing (CORS) governance, and request sanitization rules enforced by the API Gateway and downstream microservices.

---

## 2. Cross-Origin Resource Sharing (CORS) Configuration

The API Gateway serves as the centralized CORS policy enforcement point. Direct microservice endpoints reject cross-origin requests because they are inaccessible from the public internet.

### 2.1 Gateway CORS Policy Specification
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins:
              - "http://localhost:3000"
              - "http://localhost:5173"
            allowed-methods:
              - GET
              - POST
              - PUT
              - PATCH
              - DELETE
              - OPTIONS
            allowed-headers:
              - Authorization
              - Content-Type
              - X-Correlation-Id
              - X-Requested-With
            exposed-headers:
              - X-Correlation-Id
            allow-credentials: true
            max-age: 3600
```

> [!WARNING]
> Setting `allowed-origins: "*"` while `allow-credentials: true` is strictly prohibited by browser standards and system security policy. Origins must be explicitly enumerated.

---

## 3. Mandatory Security Response Headers

All HTTP responses emitted by the platform are injected with standardized defensive security headers via a global gateway post-filter:

| Header Name | Value | Defensive Purpose |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | Prevents MIME-sniffing attacks where browsers misinterpret text as executable scripts. |
| `X-Frame-Options` | `DENY` | Eliminates Clickjacking attacks by forbidding site framing inside `<iframe>` elements. |
| `Content-Security-Policy` | `default-src 'self'; frame-ancestors 'none';` | Restricts asset loading strictly to same-origin resources; blocks unauthorized inline scripts. |
| `Strict-Transport-Security`| `max-age=31536000; includeSubDomains` | Mandates TLS/HTTPS connections and prevents SSL stripping attacks (HSTS). |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Protects URI privacy by stripping path variables when navigating to cross-origin endpoints. |
| `Permissions-Policy` | `geolocation=(), camera=(), microphone=()` | Disables browser hardware APIs that are irrelevant to task management operations. |

---

## 4. Inbound Request Sanitization and Header Stripping

### 4.1 Spoofing Prevention Filter
Downstream microservices trust headers like `X-User-Id` because they originate internally. To prevent malicious external actors from passing forged headers, the API Gateway executes a pre-routing filter:

```java
@Component
public class HeaderSanitizationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<HeaderSanitizationGatewayFilterFactory.Config> {

    private static final List<String> RESTRICTED_HEADERS = List.of(
            "X-User-Id",
            "X-User-Role",
            "X-User-Name"
    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
            RESTRICTED_HEADERS.forEach(requestBuilder::headers);
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        };
    }
}
```

---

## 5. Request Payload Size Restrictions and Verb Hardening

1. **Payload Size Guard**:
   - Authentication & Task Mutations: Maximum body size capped at **1 MB**.
   - Requests exceeding this threshold receive `HTTP 413 Payload Too Large`, shielding microservices from buffer and memory exhaustion attacks.
2. **HTTP Verb Whitelist**:
   - Supported verbs: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
   - Dangerous or unnecessary verbs (`TRACE`, `TRACK`, `CONNECT`, `HEAD`) are immediately rejected at the gateway with `HTTP 405 Method Not Allowed`.

