# Antigravity Rules Specification

**Document Identifier:** DOC-AUTO-002  
**Traceability References:** Master Project Baseline, ADR-001 to ADR-008  
**Applicability:** All AI Agents, Automation Pipelines, and Developers  
**Status:** Approved Baseline  

---

## 1. Overview
This document formalizes the binding engineering rules, architectural constraints, and behavioral policies governing all automated operations within the Task Management System codebase. Compliance is mandatory for all automated agents and human contributors.

---

## 2. Binding Architectural & Engineering Rules

### `RULE-01`: Documentation-First Discipline
- **Mandate**: No application feature code, business logic, or Maven dependency modifications may occur until all architectural, design, and automation foundation phases have been fully authored, reviewed, and signed off.
- **Enforcement**: Build pipelines reject pull requests introducing application source code without corresponding approved documentation artifacts.

### `RULE-02`: Phased Execution & Systematic Phase Reviews
- **Mandate**: Project advancement proceeds strictly sequentially. Every phase requires:
  1. Formal document authoring without truncation.
  2. Completion checklist verification.
  3. A dedicated Phase Review Report (`phase-XX-review-report.md`).
  4. Updating `project-progress.md` and `project-documentation-index.md`.
- **Enforcement**: Phase skipping is strictly prohibited.

### `RULE-03`: Zero Placeholder Policy
- **Mandate**: All documentation files, class designs, DDL schemas, OpenAPI specifications, and test cases must contain complete, production-grade content.
- **Enforcement**: Files containing `// TODO: implement later`, `TBD`, or empty stub sections fail verification gates.

### `RULE-04`: Technology Stack Inviolability
- **Backend Baseline**: Java 21 LTS, Spring Boot 3.3.x, Spring Cloud 2023.x, Spring Data JPA, Hibernate, Maven.
- **Datastore Baseline**: PostgreSQL 16 with Flyway versioned migrations.
- **Messaging Baseline**: Apache Kafka 3.7+ (RabbitMQ is strictly prohibited).
- **Frontend Baseline**: React 18, TypeScript, TailwindCSS, Vite.
- **Zero Paid Cloud Dependencies**: No paid proprietary cloud services (AWS DynamoDB, Firebase, Auth0 SaaS, Confluent Cloud) may be introduced. All infrastructure runs locally via Docker Compose.

### `RULE-05`: Strict Database-per-Service Isolation
- **Mandate**: Each microservice exclusively owns its logical PostgreSQL database (`user_db`, `task_db`, `notification_db`).
- **Enforcement**:
  - Zero shared tables.
  - Zero cross-database SQL joins.
  - Zero cross-database foreign key constraints.
  - Dedicated PostgreSQL database user roles with least-privilege grants.

### `RULE-06`: Anti-Enumeration & Resource Concealment
- **Mandate**: Accessing, modifying, or deleting a task belonging to another user must return `HTTP 404 Not Found`, identical to querying a non-existent UUID.
- **Enforcement**: The system must never return `HTTP 403 Forbidden` for ID lookups, preventing malicious resource harvesting.

### `RULE-07`: Dual-Write Elimination via Transactional Outbox
- **Mandate**: Microservices must never write to a database and directly publish to Apache Kafka in the same execution without an outbox table.
- **Enforcement**: `task-service` persists domain events into the PostgreSQL `outbox_events` table within the local ACID transaction. An asynchronous poller reads and relays records to Kafka.

### `RULE-08`: Consumer Idempotency Mandate
- **Mandate**: Event consumers in `notification-service` must maintain idempotency using the `consumed_events` table.
- **Enforcement**: Duplicate Kafka deliveries must be detected, safely acknowledged, and prevented from creating duplicate notifications.

### `RULE-09`: Universal Requirement Traceability
- **Mandate**: Every requirement must maintain end-to-end traceability across `requirements-traceability-matrix.md`.
- **Enforcement**: Requirements follow taxonomy: `BR-xxx` $\rightarrow$ `FR-xxx`/`NFR-xxx` $\rightarrow$ `US-xxx` $\rightarrow$ `UC-xxx` $\rightarrow$ `API-xxx` $\rightarrow$ `DBR-xxx` $\rightarrow$ `EVT-xxx` $\rightarrow$ `TC-xxx`.

### `RULE-10`: Local Resource Budget Cap (16GB Machine)
- **Mandate**: The entire Docker Compose environment (all 5 microservices, PostgreSQL, Kafka, Zookeeper, Eureka, Prometheus, Grafana, React UI) must execute reliably within **5.25 GB total RAM allocation**.
- **Enforcement**: Strict JVM memory flags (`-Xms128m -Xmx256m` / `-Xmx384m`) and Docker container memory limits (`mem_limit`).

