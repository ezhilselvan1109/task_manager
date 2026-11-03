# Project Agent Directives: Task Management System (Microservices Architecture)

Please refer to [GEMINI.md](file:///d:/learning/task_manager/GEMINI.md) for the authoritative master agent directives.

### Quick Reference: Invariant Engineering Rules
- **RULE-01**: Documentation-First & Alignment (`documentation/` is the single source of truth).
- **RULE-02**: Zero Placeholders (no stubs, no `// TODO`, no `TBD`).
- **RULE-03**: Strict Database-per-Service (`user_db`, `task_db`, `notification_db`).
- **RULE-04**: Java 21 LTS, Spring Boot 3.3.4, Apache Kafka 3.7+ (KRaft), PostgreSQL 16, Vite React 18 TS. No RabbitMQ.
- **RULE-05**: Anti-Enumeration Security (return `404 Not Found` on cross-tenant ID access).
- **RULE-06**: Transactional Outbox Pattern (`outbox_events` table for reliable Kafka emission).
- **RULE-07**: Consumer Idempotency (`consumed_events` table in `notification-service`).
- **RULE-08**: TDD Red-Green-Refactor with $\ge 80\%$ test coverage.
- **RULE-09**: Correlation ID propagation (`X-Correlation-ID`) across HTTP and Kafka headers.
- **RULE-10**: Max 5.25 GB total RAM allocation for full Docker Compose stack.

