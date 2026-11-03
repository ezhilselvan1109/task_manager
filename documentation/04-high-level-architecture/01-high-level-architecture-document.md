# High-Level Architecture Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-001 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-INIT-002`, `DOC-REQ-003`, `REG-RTM-001` |

---

## 1. Architectural Philosophy & Principles
The **Task Management System (TMS)** architecture is grounded in Domain-Driven Design (DDD) bounded contexts, reactive asynchronous event messaging, strict multi-tenant isolation, and resilient microservices patterns.

```mermaid
flowchart TB
    subgraph Client Layer
        Browser["React 18 SPA (TypeScript)\nSingle Page Application"]
    end

    subgraph Ingress & Discovery
        GW["Spring Cloud Gateway (:8080)\n• JWT Authentication & Claims Extraction\n• Header Injection (X-User-Id)\n• Central Rate Limiting & Routing"]
        Eureka["Eureka Service Registry (:8761)\n• Heartbeat & Service Health Tracking"]
    end

    subgraph Autonomous Microservices
        US["User Service (:8081)\n• Identity, Password Hashing, JWT"]
        TS["Task Service (:8082)\n• Task CRUD, Status Engine, Metrics\n• Transactional Outbox Writer"]
        NS["Notification Service (:8083)\n• In-App Notifications\n• Idempotent Kafka Consumer"]
    end

    subgraph Data Tier (Database-per-Service)
        UDB[("PostgreSQL\nuser_db (:5432)")]
        TDB[("PostgreSQL\ntask_db (:5432)")]
        NDB[("PostgreSQL\nnotification_db (:5432)")]
    end

    subgraph Event Streaming Backbone
        Kafka["Apache Kafka 3.7+ (:9092)\nTopic: task.events.v1\nDLT: task.events.v1.DLT"]
    end

    Browser -->|HTTPS / REST| GW
    GW <-->|Heartbeats & Lookup| Eureka
    US <-->|Heartbeats| Eureka
    TS <-->|Heartbeats| Eureka
    NS <-->|Heartbeats| Eureka

    GW -->|Routes /api/v1/auth & /users| US
    GW -->|Routes /api/v1/tasks| TS
    GW -->|Routes /api/v1/notifications| NS

    US --> UDB
    TS --> TDB
    NS --> NDB

    TS -.->|1. Write to outbox_events table (ACID)| TDB
    TS -.->|2. Async Poller Publishes| Kafka
    Kafka -.->|3. Consumer Group Ingests| NS
```

---

## 2. Core Microservice Architectural Patterns

### 2.1 Database-per-Service (Pattern)
- **Principle**: Each microservice exclusively owns its database. `user-service` manages `user_db`, `task-service` manages `task_db`, and `notification-service` manages `notification_db`.
- **Enforcement**: No shared tables, views, or cross-database foreign keys. Services cannot query another service’s database directly; data dependencies must flow via REST APIs or Kafka event projections.

### 2.2 API Gateway Pattern
- **Principle**: A unified edge entry point implemented with Spring Cloud Gateway handles cross-cutting concerns:
  - Reverse proxy routing to dynamically registered Eureka service instances.
  - Stateless JWT signature validation and expiry checks.
  - Extraction of the token subject (`sub`) and injection of the trusted `X-User-Id` downstream header.
  - Global CORS policies and edge error response sanitization.

### 2.3 Service Discovery Pattern
- **Principle**: Spring Cloud Netflix Eureka provides dynamic service registration.
- **Enforcement**: Downstream services register on startup. Gateway routes traffic dynamically via logical service names (`lb://task-service`) rather than hardcoded IP addresses.

### 2.4 Transactional Outbox Pattern
- **Problem**: Dual-writes (saving to DB and directly publishing to Kafka) cause inconsistency if the broker is unreachable.
- **Solution**: `task-service` writes task mutations and domain event envelopes to an `outbox_events` table within the same PostgreSQL ACID transaction. A background Spring `@Scheduled` worker polls pending outbox records and publishes them to Kafka with broker acknowledgment.

### 2.5 Idempotent Consumer Pattern
- **Problem**: Network retries or Kafka consumer rebalancing can deliver identical messages multiple times.
- **Solution**: `notification-service` records every consumed `event_id` in a `consumed_events` table within the same transaction that generates the user notification. Duplicate events are silently skipped.

---

## 3. High-Level Architectural Cross-Cutting Concerns
1. **Security**: Zero-trust internal network model. Passwords hashed with BCrypt. Stateless JWT tokens. Gateway-injected tenant context.
2. **Resilience**: Independent failure domains. If Kafka or Notification Service degrades, Task Service continues without user-facing disruption.
3. **Observability**: Prometheus metrics scraped via Micrometer on `/actuator/prometheus`. Correlation IDs (`X-Correlation-Id`) propagated across all HTTP and Kafka interactions.

---

## 4. Review Checklist
- [x] Clear architectural philosophy and core patterns detailed.
- [x] Comprehensive topology diagram visualizing React, Gateway, Eureka, Microservices, DBs, and Kafka.
- [x] Zero-shared database and transactional outbox guarantees articulated.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline High-Level Architecture document. |

