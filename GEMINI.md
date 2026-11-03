# Project Agent Directives: Task Management System (Microservices Architecture)

## 1. Project Context & Architectural Baseline
- **Application**: Multi-tenant Task Management System built with Java 21 and React 18.
- **Repository**: `D:\learning\task_manager`
- **Architecture**: Strict Microservices with Database-per-Service, Eureka Service Discovery, Spring Cloud Gateway, and Apache Kafka Event-Driven Architecture.
- **Approved SDLC Baseline**: All architectural decisions (`ADR-001` through `ADR-008`), schemas, APIs, and runbooks are finalized in `documentation/`. Always consult these documents before modifying or adding code.

## 2. Inviolable Core Rules
1. **`RULE-01`: Documentation-Aligned Implementation**: All code, schemas, and configurations must match the approved SDLC documentation (`documentation/`). Deviations require formal ADR updates.
2. **`RULE-02`: Zero Placeholder Policy**: Never emit stubs, `// TODO`, `FIXME`, or unverified mock code in production classes or test suites. All implementations must be production-ready.
3. **`RULE-03`: Strict Database-per-Service**: 
   - `user-service` $\rightarrow$ `user_db`
   - `task-service` $\rightarrow$ `task_db`
   - `notification-service` $\rightarrow$ `notification_db`
   - Zero cross-service database access, zero cross-service SQL joins, zero shared schemas.
4. **`RULE-04`: Technology Stack Inviolability**:
   - Runtime: Java 21 LTS, Eclipse Temurin 21 Alpine JRE.
   - Backend: Spring Boot 3.3.4, Spring Cloud 2023.0.3, Spring Data JPA, Hibernate 6.5.x, Maven.
   - Persistence: PostgreSQL 16 with versioned Flyway migrations (`V1__...sql`).
   - Messaging: Apache Kafka 3.7+ (KRaft mode). **RabbitMQ is strictly prohibited**.
   - Frontend: React 18, TypeScript 5, Vite, TailwindCSS.
   - Infrastructure: Docker Compose, Prometheus, Grafana, Jenkins. **Zero paid third-party cloud services or SaaS APIs**.
5. **`RULE-05`: Anti-Enumeration & Resource Concealment**:
   - Unauthorized access to an existing resource belonging to another tenant MUST return `HTTP 404 Not Found` (never `403 Forbidden`). Always filter queries by both `id` AND authenticated `userId`.
6. **`RULE-06`: Transactional Outbox Pattern**:
   - Never write to a database and directly publish to Kafka within the same thread without the Outbox. `task-service` writes events to `outbox_events` in the local ACID transaction; an asynchronous worker drains the table.
7. **`RULE-07`: Consumer Idempotency**:
   - Event consumers in `notification-service` must deduplicate messages using the `consumed_events` table within the same transaction that updates notification state.
8. **`RULE-08`: TDD Red-Green-Refactor**:
   - Write failing unit/slice tests (`src/test/java`) before implementing production logic. Every service must achieve $\ge 80\%$ line and $\ge 75\%$ branch coverage.
9. **`RULE-09`: Correlation ID & MDC Tracing**:
   - Spring Cloud Gateway injects `X-Correlation-ID`. All services propagate this ID in SLF4J MDC and in Kafka message headers (`correlationId`).
10. **`RULE-10`: Strict 5.25 GB Total RAM Budget**:
    - Microservices run with `-Xms128m -Xmx256m` using G1GC. All 11 containers in `docker-compose.yml` must execute stably within 5.25 GB RAM on a 16 GB developer workstation.

## 3. Automated Execution with Review Protocol
When executing implementation steps:
- Execute **one defined step at a time**.
- Run compilation, Flyway migrations, and tests before claiming completion.
- Write a formal review report in `documentation/implementation/phase-16/step-XX-...md`.
- Update `documentation/project-progress.md` after each step.
- Stop and request confirmation for destructive actions, schema redesigns, or external credentials.

## 4. Skills & Workflows Reference
- Skills are cataloged in `.agents/skills/<name>/SKILL.md`.
- Workflows are cataloged in `.agents/workflows/<name>.md`.
- Detailed rules are modularized in `.agents/rules/*.md`.

