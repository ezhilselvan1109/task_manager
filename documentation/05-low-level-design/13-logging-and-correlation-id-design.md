# Logging and Correlation ID Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-013 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect & DevOps Lead |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-ARCH-009` |

---

## 1. Overview & Structured Logging Standard
The Task Management System enforces structured, contextual, and auditable logging across all microservices using **SLF4J with Logback**. Every log line generated in response to an API call or event consumer execution includes a unique distributed **Correlation ID** and tenant **User ID** stored in SLF4J’s **Mapped Diagnostic Context (MDC)**.

---

## 2. Standard Log Pattern & MDC Context

```
%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} [%thread] %-5level %logger{36} [traceId=%X{correlationId}, userId=%X{userId}] - %msg%n
```

### Sample Log Output
```
2026-11-03T14:45:12.104Z [http-nio-8082-exec-3] INFO  c.c.t.s.i.TaskServiceImpl [traceId=c1a2b3c4-d5e6-7890-abcd-ef1234567890, userId=550e8400-e29b-41d4-a716-446655440000] - Created task 7c9e6679-7425-40de-944b-e07fc1f90ae7 with status TODO and priority HIGH
```

---

## 3. Correlation ID Filter Implementation (`UserContextFilter.java`)

```java
package com.chellaa.task_service.context;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserContextFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String correlationId = req.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String userIdStr = req.getHeader(USER_ID_HEADER);

        try {
            MDC.put("correlationId", correlationId);
            res.setHeader(CORRELATION_ID_HEADER, correlationId);

            if (userIdStr != null && !userIdStr.isBlank()) {
                UUID userId = UUID.fromString(userIdStr);
                MDC.put("userId", userId.toString());
                UserContextHolder.setContext(new UserContext(userId, correlationId));
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
            UserContextHolder.clear();
        }
    }
}
```

---

## 4. Sensitive Data Masking Policy
- **Passwords**: Raw passwords must never be logged. The `RegisterRequest` and `LoginRequest` `toString()` representations suppress password fields or replace them with `[PROTECTED]`.
- **JWT Tokens**: Authorization header tokens must never be logged in raw form; log only token expiration or truncated signature snippets (`eyJhbGci...[truncated]`).
- **PII Protection**: Full email addresses may only be logged at `DEBUG` level for troubleshooting.

---

## 5. Review Checklist
- [x] Standard Logback pattern with MDC `traceId` and `userId` documented.
- [x] `UserContextFilter` with high precedence and `MDC.clear()` cleanup specified.
- [x] Strict credential and token masking policies defined.

---

## 6. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Logging and Correlation ID Design. |

