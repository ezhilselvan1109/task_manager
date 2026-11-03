# Logging Strategy and MDC Tracing

**Document Identifier:** DOC-OBS-002  
**Traceability References:** NFR-004, SEC-008, DOC-OBS-001  
**Frameworks:** Slf4j, Logback, Spring Boot Logging  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the centralized logging architecture, Logback configuration, Slf4j Mapped Diagnostic Context (MDC) attributes, and automated credential masking patterns across all microservices.

---

## 2. Standardized Log Level Policy

| Log Level | Operational Meaning | When to Log | Production Action |
|---|---|---|---|
| **ERROR** | Critical failure, unhandled runtime exception, database drop, Kafka failure. | Catch blocks in exception handlers; Kafka unrecoverable errors. | Triggers PagerDuty / Slack on-call alert. |
| **WARN** | Recoverable degradation, duplicate events, invalid credentials, circuit breaker state change. | Duplicate event in consumer; failed login attempt; circuit trips. | Monitored on Grafana error rate graph. |
| **INFO** | Normal business milestone, startup/shutdown, state transitions. | User registered; task completed; outbox batch published. | Preserved for auditing and analytics. |
| **DEBUG** | Low-level developer diagnostic detail (SQL parameters, cache hits). | Query predicate construction, HTTP request headers. | Disabled in production; enabled locally. |

---

## 3. Slf4j Mapped Diagnostic Context (MDC) Specification

Every log line automatically carries distributed context populated via filters:

| MDC Key | Data Type | Source | Purpose |
|---|---|---|---|
| `correlationId` | UUID String | Injected by API Gateway (`X-Correlation-Id`) | Traces a transaction across Gateway, Services, and Kafka. |
| `userId` | UUID String | Extracted from verified JWT claims | Correlates user-specific actions across services. |
| `serviceName` | String | Spring application property (`spring.application.name`) | Identifies producing service container. |

---

## 4. Sensitive Data Masking Pattern (`logback-spring.xml`)

To enforce `SEC-008` (Zero credential exposure in logs), Logback applies regex masking to scrub sensitive tokens and passwords before printing to console:

```xml
<configuration>
    <!-- Conversion rule to mask passwords and tokens in log messages -->
    <conversionRule conversionWord="maskedMsg" 
                    converterClass="com.taskmanager.common.logging.MaskingPatternConverter" />

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [${springAppName:-},%X{correlationId:-},%X{userId:-}] %logger{36} - %maskedMsg%n
            </pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

- **Masking Rules**:
  - `(?i)password["']?\s*[:=]\s*["']?([^"',\s]+)` $\rightarrow$ `password=******`
  - `(?i)Bearer\s+([a-zA-Z0-9_\-\.]+)` $\rightarrow$ `Bearer [MASKED_JWT]`

