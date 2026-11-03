# Project Assumptions: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-INIT-008 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Senior Business Analyst & Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Requirements** | BRULE-001 to BRULE-010, BR-001 to BR-010 |

---

## 1. Overview
Project assumptions represent conditions and premises believed to be true during planning. Documenting assumptions explicitly ensures they are validated rather than silently integrated as confirmed facts. If an assumption is invalidated, its impact is traced through dependent phases.

---

## 2. Master Assumptions Register

| Assumption ID | Category | Assumption Statement | Validation Strategy | Impact / Risk if Invalid | Linked Requirements |
|---|---|---|---|---|---|
| **ASM-001** | Business Scope | The system MVP is designed strictly for individual single-user task management. Multi-user shared boards or organization roles are not required. | Phase 2 PRD sign-off | Moderate. Adding multi-tenancy sharing requires ACL tables and complex authorization policies. | BR-001, BR-004, BRULE-001 |
| **ASM-002** | Data Types | All entity primary identifiers (`id`, `userId`) will be standard UUID (v4) represented as 36-character strings in APIs and UUID native types in PostgreSQL. | Phase 6 Database Design | Low. Auto-increment Long IDs expose record volumes; UUIDs are architecturally superior for distributed systems. | BRULE-006, DBR-001 |
| **ASM-003** | Timezones | All timestamps (`createdAt`, `updatedAt`, `dueDate`, `readAt`) are captured, processed, and stored strictly in UTC (ISO-8601 format). Timezone localization is exclusively handled on the frontend. | API & Database reviews | High. Storing local timezones causes subtle date-boundary bugs in overdue calculations and event orderings. | BRULE-007, DBR-002 |
| **ASM-004** | Communication | External HTTP clients communicate exclusively through `api-gateway` on port `8080`. Downstream services (`8081`, `8082`, `8083`) are isolated from direct public traffic. | Phase 4 Architecture & Docker Compose network config | Critical. Bypassing the gateway exposes internal services without centralized JWT authentication. | NFR-001, SEC-001 |
| **ASM-005** | Messaging Broker | Apache Kafka is the sole asynchronous event broker. Message delivery must handle at-least-once semantics through idempotent consumer designs. | Phase 8 Kafka Design & Consumer testing | High. Without consumer deduplication, duplicate task notifications would be displayed to users. | EVT-001, EVT-002 |
| **ASM-006** | Database Boundary | Microservices must never share database schemas or access tables owned by another service. All inter-service data dependencies flow via REST APIs or Kafka events. | Architecture review & Phase 6 ERDs | Critical. Cross-service joins violate bounded context isolation and defeat microservices independence. | NFR-003, ADR-001 |
| **ASM-007** | Transactional Outbox | Direct dual-writes (committing to DB and then calling `kafkaTemplate.send()`) cause data inconsistency during network partitions. The Transactional Outbox pattern is assumed mandatory for `task-service`. | Phase 5 LLD & Phase 8 Event Design | High. Without an Outbox, a database commit can succeed while the Kafka event fails, dropping notification generation. | EVT-006, NFR-005 |
| **ASM-008** | Notification Channel | MVP notification delivery is purely in-app via polling/REST queries. External delivery (Email, SMS, Web Push) is not implemented in the MVP. | Phase 1 Scope & Phase 2 SRS | Low. Scope boundary protection. | BR-008, FR-018 |
| **ASM-009** | Infrastructure Budget | All infrastructure components (PostgreSQL, Kafka, Eureka, Jenkins, Prometheus, Grafana) run in Docker on local or dedicated virtual hosts without paid cloud dependencies. | Phase 11 Docker Compose testing | Medium. Must tune JVM heap and container memory limits to avoid host OOM conditions. | NFR-002, NFR-004 |
| **ASM-010** | Client State | The frontend React SPA is responsible for persisting JWT tokens securely (e.g. in-memory with refresh token in secure HTTP-only cookie or controlled storage) and injecting Authorization headers. | Phase 7 API Design & Phase 9 Security | High. Insecure client storage exposes tokens to XSS attacks. | SEC-002, API-001 |

---

## 3. Review Checklist
- [x] All 10 baseline assumptions uniquely identified with `ASM-xxx`.
- [x] Cross-functional categories (Business, Data, Architecture, Security, Infrastructure) covered.
- [x] Validation strategies and risk impact defined for each item.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Senior Business Analyst | Initial baseline assumptions register. |

