# Database Normalization Analysis: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-DB-009 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Database Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-DB-004`, `DOC-DB-005` |

---

## 1. Executive Summary & Normalization Target
Relational schemas across the Task Management System are normalized to **Third Normal Form (3NF)** and **Boyce-Codd Normal Form (BCNF)** to eliminate data redundancy, prevent insertion/update/deletion anomalies, and maintain absolute data integrity, with controlled denormalization applied only to event payloads for performance.

---

## 2. Normalization Evaluation by Form

### 2.1 First Normal Form (1NF) Compliance
- **Rule**: Every column contains atomic (indivisible) values; no repeating groups or comma-separated lists.
- **Analysis**:
  - `tasks`: Each column (`id`, `user_id`, `title`, `description`, `status`, `priority`, `due_date`, `is_deleted`) holds a single atomic scalar value.
  - `users`: Email, password hash, and full name are distinct atomic fields.
  - *Status*: **100% 1NF Compliant**.

### 2.2 Second Normal Form (2NF) Compliance
- **Rule**: Must be in 1NF and have zero partial dependencies (all non-key attributes must be fully functionally dependent on the entire primary key).
- **Analysis**:
  - In all tables (`tasks`, `users`, `refresh_tokens`, `notifications`, `outbox_events`, `consumed_events`), the primary key is a single surrogate attribute (`id` or `event_id`).
  - Because all primary keys are single-column UUIDs without composite keys, partial key dependencies are mathematically impossible.
  - *Status*: **100% 2NF Compliant**.

### 2.3 Third Normal Form (3NF) Compliance
- **Rule**: Must be in 2NF and have zero transitive dependencies ($X \rightarrow Y$ where $Y \rightarrow Z$ and $X$ is the primary key).
- **Analysis**:
  - `tasks`: Primary key $id \rightarrow \{user\_id, title, status, priority, due\_date\}$. None of the non-key attributes determine another non-key attribute. For instance, `status` does not determine `priority`, nor does `due_date` determine `status`.
  - `users`: $id \rightarrow \{email, password\_hash, full\_name, status\}$.
  - *Status*: **100% 3NF Compliant**.

---

## 3. Deliberate Controlled Denormalization: `outbox_events.payload`
- **Pattern**: Storing the domain event envelope as a serialized `JSON` string / `JSONB` document inside `outbox_events.payload`.
- **Justification**:
  1. *Decoupling from Future Entity Changes*: If a task entity is modified or soft deleted later, the event payload captures an immutable point-in-time snapshot of the entity at the exact moment of the event.
  2. *Kafka Publishing Throughput*: The outbox poller does not need to perform complex relational joins or re-serialize domain models; it reads the pre-computed payload and streams it directly to Kafka.
  3. *Zero Relational Coupling*: Prevents the outbox table from breaking 3NF rules of other tables.

---

## 4. Review Checklist
- [x] Rigorous functional dependency analysis across 1NF, 2NF, 3NF, and BCNF.
- [x] Mathematical proof of 2NF compliance via single-column UUID primary keys.
- [x] Clear architectural justification for event payload denormalization.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Database Architect | Initial baseline Database Normalization Analysis. |

