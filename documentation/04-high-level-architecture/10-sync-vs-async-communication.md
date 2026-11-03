# Synchronous vs. Asynchronous Communication Decision: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-010 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-ARCH-009`, `DOC-ARCH-011` |

---

## 1. Context & Architectural Challenge
In a microservices architecture, selecting communication mechanisms determines system availability, latency characteristics, temporal coupling, and failure blast radius. When designing the interaction between `task-service` and `notification-service`, the architecture team evaluated two primary paradigms:
1. **Synchronous Inter-Service HTTP/REST**: Task Service calls Notification Service directly via an HTTP client (e.g. OpenFeign / WebClient) during task creation or state changes.
2. **Asynchronous Event-Driven Messaging (Kafka)**: Task Service persists events to a local outbox; events stream asynchronously to Notification Service via Apache Kafka.

---

## 2. Trade-Off Analysis Matrix

| Evaluation Dimension | Option 1: Synchronous HTTP (OpenFeign) | Option 2: Asynchronous Kafka Event Streaming (Selected) | Winning Rationale |
|---|---|---|---|
| **Temporal Coupling** | **High**: Both services must be available at the exact same millisecond. | **Zero**: Task Service operates independently even if Notification Service is offline or restarting. | Asynchronous isolates failure domains; user task creation never fails due to notification downtime. |
| **User-Facing Latency** | **Additive**: Task response time includes Notification Service execution time plus network overhead ($\approx +50\text{ms}$ to $150\text{ms}$). | **Minimal**: Task Service commits task and outbox record locally in single DB transaction; returns in $< 20\text{ms}$. | Sub-second user interaction requirement met. |
| **Availability & SLAs** | **Multiplicative Degradation**: If each service has 99.5% availability, compound availability drops to $99.5\% \times 99.5\% = 99.0\%$. | **Independent**: Compound availability remains 99.9% for core task operations. | Meets `NFR-004` (99.9% availability SLA). |
| **Failure Recovery** | **Complex**: Requires manual circuit breakers, retries, and fallback persistence to prevent lost notifications. | **Native**: Kafka durable broker log and consumer offset management handle retries and replayability automatically. | Built-in fault tolerance. |
| **Operational Complexity** | **Low**: Requires no message broker infrastructure. | **Moderate**: Requires Kafka broker container, topic management, and consumer offset tracking. | Local Docker Compose makes Kafka container setup straightforward and manageable. |

---

## 3. Architecture Decision Outcome
- **Synchronous REST**: Retained strictly for **Command/Query Ingress** where the user requires an immediate synchronous answer (e.g., login token, fetching task lists, dashboard metric calculations).
- **Asynchronous Kafka**: Mandated for **Domain Event Propagation** (e.g., task created, updated, completed, deleted) to eliminate temporal coupling and ensure zero data loss.

---

## 4. Review Checklist
- [x] Clear trade-off analysis across temporal coupling, latency, availability, and recovery.
- [x] Quantitative rationale documented for selecting asynchronous Kafka over synchronous REST for domain events.
- [x] Architectural alignment with high availability and resilience SLAs verified.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline Synchronous vs Asynchronous Communication Decision document. |

