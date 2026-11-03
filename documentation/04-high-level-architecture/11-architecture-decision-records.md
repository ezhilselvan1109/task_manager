# Architecture Decision Records (ADRs): Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-011 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-010` |

---

## 1. Overview
Architecture Decision Records (ADRs) capture significant architectural choices, context, evaluated alternatives, consequences, and compliance mandates across the Task Management System lifecycle.

---

## 2. Master ADR Index

| ADR ID | Decision Title | Status | Deciding Roles | Link to Detailed Record |
|---|---|:---:|---|---|
| **ADR-001** | Microservices Architecture & Database-per-Service | Approved | Solution Architect, Lead BA | [ADR-001](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-001-microservices-and-database-per-service.md) |
| **ADR-002** | Selection of Apache Kafka over RabbitMQ for Event Streaming | Approved | Solution Architect, DevOps Lead | [ADR-002](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-002-apache-kafka-for-event-messaging.md) |
| **ADR-003** | Spring Cloud Gateway with Stateless JWT Authentication | Approved | Security Architect, Solution Architect | [ADR-003](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-003-spring-cloud-gateway-and-stateless-jwt.md) |
| **ADR-004** | PostgreSQL 16 Relational Engine & Flyway Migration | Approved | Database Architect, Software Architect | [ADR-004](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-004-postgresql-and-flyway-migration.md) |
| **ADR-005** | Transactional Outbox Pattern for Resilient Event Publishing | Approved | Software Architect, Solution Architect | [ADR-005](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-005-transactional-outbox-pattern.md) |
| **ADR-006** | Idempotent Consumer Design for Notification Service | Approved | Software Architect, QA Lead | [ADR-006](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-006-idempotent-consumer-design.md) |
| **ADR-007** | Spring Cloud Netflix Eureka for Dynamic Service Discovery | Approved | DevOps Lead, Solution Architect | [ADR-007](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-007-eureka-for-service-discovery.md) |
| **ADR-008** | React 18 & TypeScript for Frontend Single Page Application | Approved | Product Manager, Frontend Lead | [ADR-008](file:///d:/learning/task_manager/documentation/architecture-decisions/ADR-008-react-typescript-spa-frontend.md) |

---

## 3. Review Checklist
- [x] All 8 foundational architecture decisions cataloged with unique IDs (`ADR-001` to `ADR-008`).
- [x] Clear mapping to detailed ADR files in `documentation/architecture-decisions/`.
- [x] Alignment with master project baseline and zero third-party paid service constraints verified.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline ADR summary catalog. |

