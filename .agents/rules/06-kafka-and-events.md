# Rule 06: Apache Kafka & Event-Driven Architecture Standards

## Kafka Messaging Rules
1. **Topic Conventions**:
   - Main topic: `task-events` (3 partitions, replication factor 1 for dev / 3 for prod).
   - Dead-letter topic: `task-events.DLT` (1 partition).
   - Partition key: `aggregateId` (`taskId`) to guarantee strict in-order processing per task.
2. **Standard Event Envelope**:
   - All events published to `task-events` must conform to the unified envelope:
     - `eventId` (UUID)
     - `eventType` (`TASK_CREATED`, `TASK_UPDATED`, `TASK_COMPLETED`, `TASK_DELETED`)
     - `eventVersion` (`1.0`)
     - `aggregateId` (Task UUID)
     - `occurredAt` (UTC ISO-8601)
     - `producer` (`task-service`)
     - `correlationId` (UUID)
     - `payload` (JSON object containing task state attributes)
3. **Dual-Write Elimination via Transactional Outbox**:
   - `task-service` MUST NOT publish directly to Kafka inside the HTTP request thread.
   - Domain state changes and outbox event insertions into `outbox_events` must occur within the same local `@Transactional` block.
   - An asynchronous poller (`@Scheduled`) queries unpublished events using `SELECT ... FOR UPDATE SKIP LOCKED`, emits to Kafka, and marks them `PROCESSED`.
4. **Consumer Idempotency**:
   - `notification-service` MUST record incoming `eventId` in the `consumed_events` table within the same transaction that stores the notification.
   - If `eventId` already exists (unique constraint violation), the event is silently acknowledged (`ack.acknowledge()`) and dropped to prevent duplicate notifications.
5. **Dead-Letter Handling**:
   - Consumers must configure a `SeekToCurrentErrorHandler` / `DefaultErrorHandler` with exponential backoff (e.g. 3 attempts).
   - Unrecoverable messages after 3 failed attempts must be forwarded to `task-events.DLT` with error metadata headers.

