# Workflow 06: Transactional Outbox Implementation

## 1. Overview & Purpose
Defines the implementation of the Transactional Outbox pattern in `task-service` to eliminate dual-write hazards and ensure eventual consistency with Kafka.

- **Workflow ID**: `WF-006`
- **Trigger**: Implementation of task mutation operations that trigger domain events.

## 2. Preconditions
- `outbox_events` table created in `task_db` via Flyway.
- `TaskOutboxEvent` JPA entity and repository defined.

## 3. Required Inputs
- Task domain mutation operation (create, update, complete, delete).
- Event type and JSON payload.

## 4. Execution Steps
1. **Persist Within Local Transaction**:
   - In `TaskServiceImpl`, inside `@Transactional`:
     - Save task domain entity.
     - Serialize event payload into JSON.
     - Save `TaskOutboxEvent` entity with status `PENDING` into `outbox_events`.
2. **Implement Concurrency-Safe Outbox Poller**:
   - Create `@Scheduled(fixedDelay = 500)` publisher worker (`TaskOutboxPublisher`).
   - Query pending records with `SELECT ... FOR UPDATE SKIP LOCKED LIMIT 50`.
3. **Publish to Kafka**:
   - For each locked outbox record:
     - Publish to topic `task-events` with task ID as partition key.
     - On successful ack, update status to `PROCESSED` with timestamp.
     - On failure, increment retry count or mark `FAILED`.
4. **Implement Cleanup Job**:
   - Create scheduled job purging `PROCESSED` outbox events older than 7 days.

## 5. Skills to Use
- [`transactional-outbox-idempotency`](file:///d:/learning/task_manager/.agents/skills/transactional-outbox-idempotency/SKILL.md)
- [`kafka-producer-consumer`](file:///d:/learning/task_manager/.agents/skills/kafka-producer-consumer/SKILL.md)
- [`spring-data-jpa`](file:///d:/learning/task_manager/.agents/skills/spring-data-jpa/SKILL.md)

## 6. Rules to Follow
- [Rule 06: Apache Kafka & Event-Driven Architecture Standards](file:///d:/learning/task_manager/.agents/rules/06-kafka-and-events.md)
- [Rule 04: Database & Flyway Standards](file:///d:/learning/task_manager/.agents/rules/04-database-and-flyway.md)

## 7. Tests and Validation
- Integration test simulating Kafka broker outage; verify task saves and outbox record remains `PENDING`.
- Resume Kafka broker; verify poller drains outbox record to `PROCESSED`.

## 8. Failure Handling
- If poller deadlocks, verify `SKIP LOCKED` clause is in native query.
- If outbox accumulates lag, alert via Prometheus metric `outbox_events_pending_total`.

## 9. Review Checklist
- [ ] Task and outbox persistence share single local ACID transaction.
- [ ] Direct KafkaTemplate call is absent from controller/service thread.
- [ ] Poller query uses `FOR UPDATE SKIP LOCKED`.

## 10. Expected Outputs
- Outbox persistence logic in service.
- Scheduled outbox poller component.
- Outbox integration test.

