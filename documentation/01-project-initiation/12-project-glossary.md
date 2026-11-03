# Project Glossary: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-INIT-012 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Technical Documentation Specialist & Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Requirements** | All BRs, FRs, NFRs |

---

## 1. Overview
The Project Glossary establishes an authoritative, unified vocabulary across all business, architectural, engineering, and quality documentation. Consistent terminology prevents semantic ambiguity between team members and system layers.

---

## 2. Business & Domain Terminology

| Term | Category | Definition |
|---|---|---|
| **Task** | Domain Entity | The fundamental unit of personal work created by a user, containing a title, description, status, priority, and optional due date. |
| **Task Status** | Domain Enum | The discrete lifecycle state of a task: `TODO` (pending initiation), `IN_PROGRESS` (actively being worked on), `COMPLETED` (successfully finished), `CANCELLED` (abandoned without completion). |
| **Task Priority** | Domain Enum | The urgency/importance classification of a task: `LOW`, `MEDIUM` (default), `HIGH`, `URGENT`. |
| **Overdue Task** | Domain Concept | A task whose `dueDate` is strictly earlier than the current UTC timestamp and whose status is neither `COMPLETED` nor `CANCELLED`. Evaluated dynamically on read operations. |
| **In-App Notification** | Domain Entity | A persistent record generated in response to a task lifecycle event, intended for viewing by the task owner within the web application. |
| **Productivity Summary** | Business Metric | Aggregated counts of user tasks grouped by status and priority, presented on the user's dashboard. |
| **User Profile** | Domain Entity | The account information of an authenticated user, including unique email, display name, and registration timestamp. |

---

## 3. Architecture & Technical Terminology

| Term | Category | Definition |
|---|---|---|
| **API Gateway** | Architectural Pattern | A single reverse-proxy entry point (`api-gateway`) that receives all client traffic, enforces authentication, and routes requests to downstream microservices. |
| **Service Discovery** | Infrastructure | A centralized registry (Spring Cloud Netflix Eureka) enabling microservices to dynamically locate each other by logical service name rather than static IP addresses. |
| **Database-per-Service** | Architectural Pattern | A microservices design pattern where each autonomous service owns and manages its dedicated database (`user_db`, `task_db`, `notification_db`), strictly prohibiting foreign schema access. |
| **Transactional Outbox** | Resiliency Pattern | A pattern where domain events are stored in a database table (`outbox_events`) within the same database transaction as the business entity change, guaranteeing zero event loss. |
| **Idempotent Consumer** | Resiliency Pattern | A message consumer design that safely handles duplicate event deliveries without unintended duplicate side effects, verified by tracking unique `event_id` records in `consumed_events`. |
| **Dead Letter Topic (DLT)**| Resiliency Pattern | A dedicated Kafka topic (`task.events.v1.DLT`) where poisoned or persistently unprocessable messages are redirected after max retry exhaustion for forensic analysis. |
| **Stateless Authentication** | Security Pattern | An authentication scheme utilizing JSON Web Tokens (JWT) where the server does not store active session records; claims are cryptographically verified per request. |
| **Correlation ID** | Observability | A unique UUID generated at the API Gateway (`X-Correlation-Id`) and passed across HTTP headers and Kafka event headers to trace a transaction across all microservices. |
| **Flyway** | Data Migration | An open-source database migration tool that applies versioned SQL scripts (`V1__...sql`) deterministically on microservice startup. |

---

## 4. Operational & Tooling Acronyms

| Acronym | Expansion | Context |
|---|---|---|
| **ACID** | Atomicity, Consistency, Isolation, Durability | Database transaction properties guaranteed by PostgreSQL. |
| **ADR** | Architecture Decision Record | Standardized documentation recording significant architectural choices and rationales. |
| **BPMN** | Business Process Model and Notation | Standardized graphical notation for modeling business workflows. |
| **CRUD** | Create, Read, Update, Delete | Core data manipulation operations. |
| **DTO** | Data Transfer Object | Plain Java object used to transfer data across API boundaries without exposing entities. |
| **JPA** | Java Persistence API | Standard Java specification for Object-Relational Mapping (implemented via Hibernate). |
| **JWT** | JSON Web Token | Compact, URL-safe means of representing claims to be transferred between parties (RFC 7519). |
| **RTM** | Requirements Traceability Matrix | Cross-reference table linking requirements to design, code, and test cases. |
| **SLI / SLO** | Service Level Indicator / Service Level Objective | Quantitative measures and target service performance thresholds. |
| **SPA** | Single Page Application | Client-side web application loaded in a browser (React 18). |

---

## 5. Review Checklist
- [x] Comprehensive coverage of domain, architectural, and operational vocabulary.
- [x] Clear distinctions established between technical acronyms and domain entities.
- [x] Standard definitions aligned with all 15 SDLC phases.

---

## 6. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Technical Documentation Specialist | Initial baseline project glossary creation. |

