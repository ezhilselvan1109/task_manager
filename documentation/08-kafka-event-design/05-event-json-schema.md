# Event JSON Schema: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-EVT-005 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-EVT-004`, `DOC-API-005` |

---

## 1. Event Envelope JSON Schema (Draft 2020-12)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "TaskEventEnvelope",
  "type": "object",
  "required": [
    "eventId",
    "eventType",
    "eventVersion",
    "aggregateId",
    "userId",
    "producer",
    "occurredAt",
    "correlationId",
    "payload"
  ],
  "properties": {
    "eventId": { "type": "string", "format": "uuid" },
    "eventType": {
      "type": "string",
      "enum": ["TASK_CREATED", "TASK_UPDATED", "TASK_COMPLETED", "TASK_DELETED"]
    },
    "eventVersion": { "type": "string", "default": "1.0" },
    "aggregateId": { "type": "string", "format": "uuid" },
    "userId": { "type": "string", "format": "uuid" },
    "producer": { "type": "string", "example": "task-service" },
    "occurredAt": { "type": "string", "format": "date-time" },
    "correlationId": { "type": "string", "format": "uuid" },
    "payload": {
      "type": "object",
      "required": ["id", "title", "status", "priority"],
      "properties": {
        "id": { "type": "string", "format": "uuid" },
        "userId": { "type": "string", "format": "uuid" },
        "title": { "type": "string" },
        "description": { "type": ["string", "null"] },
        "status": { "type": "string", "enum": ["TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"] },
        "priority": { "type": "string", "enum": ["LOW", "MEDIUM", "HIGH", "URGENT"] },
        "dueDate": { "type": ["string", "null"], "format": "date-time" }
      }
    }
  }
}
```

---

## 2. Concrete JSON Event Examples

### 2.1 `TASK_CREATED`
```json
{
  "eventId": "8f3b2a1c-9d4e-4f7a-8b1e-6c5d4e3f2a1b",
  "eventType": "TASK_CREATED",
  "eventVersion": "1.0",
  "aggregateId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "producer": "task-service",
  "occurredAt": "2026-11-03T11:00:00Z",
  "correlationId": "c1a2b3c4-d5e6-7890-abcd-ef1234567890",
  "payload": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Review Microservice Security Architecture",
    "description": "Perform threat modeling on API Gateway.",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-11-10T18:00:00Z"
  }
}
```

### 2.2 `TASK_COMPLETED`
```json
{
  "eventId": "a2c3d4e5-f6a7-8901-bcde-f12345678901",
  "eventType": "TASK_COMPLETED",
  "eventVersion": "1.0",
  "aggregateId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "producer": "task-service",
  "occurredAt": "2026-11-03T16:45:00Z",
  "correlationId": "d2e3f4a5-b6c7-8901-ef12-345678901234",
  "payload": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Review Microservice Security Architecture",
    "description": "Perform threat modeling on API Gateway.",
    "status": "COMPLETED",
    "priority": "HIGH",
    "dueDate": "2026-11-10T18:00:00Z"
  }
}
```

---

## 3. Review Checklist
- [x] Full JSON Schema Draft 2020-12 envelope specification defined.
- [x] Concrete JSON examples provided for creation and completion events.
- [x] Envelope format includes `correlationId`, `aggregateId`, and `userId` fields.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Event JSON Schema. |

