# ADR-009: Migration of Relational Persistence Tier from PostgreSQL 16 to MySQL 8.4 LTS

| Field | Value |
|---|---|
| **ADR Identifier** | ADR-009 |
| **Status** | Approved (Supersedes PostgreSQL engine selection in ADR-002) |
| **Date** | 2026-11-18 |
| **Decision Makers** | Project Owner |

---

## 1. Context
The initial baseline (`ADR-002`, `RULE-04`) selected PostgreSQL 16 as the relational database engine across `user_db`, `task_db`, and `notification_db`. On 2026-11-18, the project owner formally requested migrating the relational persistence tier to **MySQL 8.4 LTS** across all microservices, Docker Compose, Kubernetes (`k3s`), and Flyway migrations, while storing UUID keys as human-readable `CHAR(36)`.

## 2. Decision
1. **Database Engine**: Standardize on **MySQL 8.4 LTS** (`mysql:8.4` container image, InnoDB storage engine, `utf8mb4` character set, `utf8mb4_0900_ai_ci` collation).
2. **Strict Database-per-Service Preserved (`RULE-03`)**: Maintain three isolated schemas with dedicated least-privilege accounts:
   - `user-service` $\rightarrow$ `user_db` (`user_app`)
   - `task-service` $\rightarrow$ `task_db` (`task_app`)
   - `notification-service` $\rightarrow$ `notification_db` (`notification_app`)
3. **UUID Storage**: Store all `UUID` primary and foreign keys as `CHAR(36)` via Hibernate `@JdbcTypeCode(SqlTypes.CHAR)`.
4. **Transactional Outbox (`RULE-06`)**: Retain `SELECT ... FOR UPDATE SKIP LOCKED` (natively supported by MySQL 8.0+/8.4 InnoDB).
5. **Reserved Keyword Safety**: Rename `notifications.read` physical column to `is_read` in MySQL DDL while preserving Java property `read` and REST JSON field `"read"`.
6. **JDBC & Flyway Drivers**: Replace `postgresql` + `flyway-database-postgresql` with `mysql-connector-j` + `flyway-mysql` across all persistence services.

## 3. Consequences
- **Positive**: Meets project owner's MySQL operational requirement; preserves 100% REST API compatibility, Database-per-Service isolation, Transactional Outbox `SKIP LOCKED` semantics, and Consumer Idempotency.
- **Trade-offs**: Existing PostgreSQL volumes on the VPS are retired and replaced by fresh MySQL 8.4 volumes initialized via Flyway `V1_0` migrations; PostgreSQL partial indexes (`WHERE ...`) are converted to standard composite B-Tree indexes (`(user_id, is_deleted, ...)`).
