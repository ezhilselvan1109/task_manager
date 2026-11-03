# Activity Diagrams: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-BA-006 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Senior Business Analyst & Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-BA-003`, `DOC-REQ-004` |

---

## 1. Overview
Activity diagrams illustrate procedural control flows, conditional decision logic, concurrent branches, and transactional commit boundaries within the Task Management System.

---

## 2. Core Activity Diagrams

### 2.1 Task Creation & Transactional Outbox Activity

```mermaid
flowchart TD
    Start([User Submits Task Form]) --> Validate{Is Title Valid?<br>1..255 chars, not blank}
    
    Validate -->|No| Reject400[Return HTTP 400 Bad Request<br>ERR_TITLE_REQUIRED] --> EndFail([End Failure])
    
    Validate -->|Yes| ExtractUser[Extract X-User-Id from Security Context]
    ExtractUser --> BeginTx[Begin PostgreSQL ACID Transaction]
    
    BeginTx --> GenUUID[Generate Task UUID v4]
    GenUUID --> InsertTask[INSERT INTO tasks<br>status=TODO, priority=MEDIUM/input, UTC timestamps]
    
    InsertTask --> SerializeEvent[Serialize TASK_CREATED Event to JSON Payload]
    SerializeEvent --> InsertOutbox[INSERT INTO outbox_events<br>event_id=UUID, processed=false]
    
    InsertOutbox --> CommitTx{Commit Transaction?}
    CommitTx -->|Error / DB Lock| Rollback[Rollback Transaction] --> Return500[Return HTTP 500] --> EndFail
    CommitTx -->|Success| CommitOk[Transaction Committed]
    
    CommitOk --> Return201[Return HTTP 201 Created with Task DTO] --> EndSuccess([End Success])
```

---

### 2.2 Outbox Poller & Kafka Publishing Activity

```mermaid
flowchart TD
    Timer([Timer Trigger: Every 1000ms]) --> PollQuery[SELECT * FROM outbox_events<br>WHERE processed = false<br>ORDER BY created_at ASC<br>LIMIT 50 FOR UPDATE SKIP LOCKED]
    
    PollQuery --> CheckRows{Pending Rows Found?}
    CheckRows -->|No| SleepPoller[Sleep 1s] --> EndPoller([Poller Idle])
    
    CheckRows -->|Yes| ForEachRow[Iterate Over Batch Rows]
    ForEachRow --> BuildRecord[Construct Kafka ProducerRecord<br>Topic: task.events.v1<br>Partition Key: aggregate_id]
    
    BuildRecord --> SendKafka[kafkaTemplate.send record]
    SendKafka --> AwaitAck{Broker ACK Received?}
    
    AwaitAck -->|Broker Timeout / Error| LogWarning[Log Warning & Increment Retry Count] --> KeepPending[Leave processed = false] --> NextBatch[Exit Loop & Retry Next Cycle]
    
    AwaitAck -->|Success ACK| UpdateOutbox[UPDATE outbox_events<br>SET processed = true, processed_at = now]
    UpdateOutbox --> HasMore{More in Batch?}
    HasMore -->|Yes| ForEachRow
    HasMore -->|No| SleepPoller
```

---

### 2.3 Kafka Event Consumer & Idempotency Activity

```mermaid
flowchart TD
    MessageArrives([Kafka Consumer Polls Record]) --> Deserialize[Deserialize JSON to TaskEventEnvelope]
    
    Deserialize --> CheckIdemp{SELECT 1 FROM consumed_events<br>WHERE event_id = :eventId}
    
    CheckIdemp -->|Found Match| LogDuplicate[Log: Duplicate Event Detected<br>Skipping Side Effects] --> AckMessage[Commit Kafka Offset] --> EndAck([End Cleanly])
    
    CheckIdemp -->|Not Found| BeginNotifTx[Begin Notification DB Transaction]
    
    BeginNotifTx --> FormatMessage[Format Notification String<br>e.g., Task 'XYZ' Completed]
    FormatMessage --> InsertNotif[INSERT INTO notifications<br>user_id, message, read=false]
    InsertNotif --> InsertConsumed[INSERT INTO consumed_events<br>event_id, consumed_at]
    
    InsertConsumed --> CommitNotifTx{Commit Transaction?}
    CommitNotifTx -->|Error| RollbackNotif[Rollback DB Transaction] --> NackKafka[Throw Exception / Retry via ErrorHandler] --> EndRetry([Trigger Kafka Retry / DLT])
    CommitNotifTx -->|Success| CommitSuccess[DB Committed] --> AckMessage
```

---

## 3. Review Checklist
- [x] Clear decision gateways (`{ ... ? }`) and branch pathways modeled.
- [x] ACID database transaction boundaries explicitly demarcated.
- [x] Retry, error handling, and duplicate suppression logic mapped.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Activity Diagrams. |

