# Distributed Tracing Specification

**Document Identifier:** DOC-OBS-004  
**Traceability References:** NFR-004, DOC-OBS-001, DOC-OBS-002  
**Tracing Mechanism:** Correlation ID (`X-Correlation-Id`), MDC, Kafka Record Headers  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the distributed tracing architecture across the Task Management platform. It defines the lifecycle, propagation mechanisms, and header serialization rules governing the `X-Correlation-Id` across synchronous HTTP calls and asynchronous Kafka messaging streams.

---

## 2. Distributed Tracing Flowchart

```mermaid
sequenceDiagram
    autonumber
    actor Client as Browser / API Caller
    participant GW as API Gateway
    participant TS as task-service
    participant DB as PostgreSQL (task_db)
    participant KF as Apache Kafka
    participant NS as notification-service

    Client->>GW: POST /api/v1/tasks (Optional: X-Correlation-Id)
    GW->>GW: If absent, generate UUID v4: "a8b3c-912f"
    GW->>GW: MDC.put("correlationId", "a8b3c-912f")
    GW->>TS: Forward HTTP Request (Header: X-Correlation-Id: "a8b3c-912f")
    
    activate TS
    TS->>TS: Interceptor places "a8b3c-912f" in local MDC
    TS->>DB: INSERT INTO tasks (...) & INSERT INTO outbox_events (correlation_id="a8b3c-912f")
    TS-->>GW: 201 Created (Header: X-Correlation-Id: "a8b3c-912f")
    deactivate TS

    GW-->>Client: 201 Created (Header: X-Correlation-Id: "a8b3c-912f")

    Note over TS,KF: Asynchronous Outbox Polling
    TS->>KF: Produce Record (Payload.correlationId = "a8b3c-912f", Header: x-correlation-id)
    
    activate NS
    KF->>NS: Consume Record
    NS->>NS: Listener extracts header -> MDC.put("correlationId", "a8b3c-912f")
    NS->>NS: Log: "Processing notification for correlationId: a8b3c-912f"
    deactivate NS
```

---

## 3. Correlation Propagation Standards

1. **HTTP Ingress Rule**:
   - If incoming request contains `X-Correlation-Id`, validate format (UUID v4 regex: `^[0-9a-fA-F-]{36}$`). If valid, retain it; if invalid or missing, generate a new UUID.
2. **HTTP Outbound Rule**:
   - Every response from API Gateway includes header:
     `X-Correlation-Id: 3fa85f64-5717-4562-b3fc-2c963f66afa6`
   - Enables users and frontend error modals to reference the exact server execution trace when submitting bug reports.
3. **Kafka Header Rule**:
   - Outbox poller injects standard byte-encoded Kafka header:
     `record.headers().add("x-correlation-id", correlationId.getBytes(StandardCharsets.UTF_8))`
4. **Log Context Hygiene**:
   - Servlets and Kafka listeners execute `MDC.remove("correlationId")` inside `finally` blocks to prevent thread-pool context leakage.

