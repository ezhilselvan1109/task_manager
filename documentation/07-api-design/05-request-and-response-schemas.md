# Request and Response Schemas: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-API-005 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | API Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-API-003`, `DOC-LLD-008` |

---

## 1. JSON Schema Standards
All schemas comply with **JSON Schema Draft 2020-12**. Properties utilize strict type validations, boundary lengths, regex patterns, and ISO formats.

---

## 2. Core Request Schemas

### 2.1 `CreateTaskRequest` Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "CreateTaskRequest",
  "type": "object",
  "required": ["title"],
  "additionalProperties": false,
  "properties": {
    "title": {
      "type": "string",
      "minLength": 1,
      "maxLength": 255,
      "description": "Non-blank title of the task"
    },
    "description": {
      "type": "string",
      "maxLength": 2000,
      "description": "Optional detailed notes"
    },
    "priority": {
      "type": "string",
      "enum": ["LOW", "MEDIUM", "HIGH", "URGENT"],
      "default": "MEDIUM",
      "description": "Priority urgency level"
    },
    "dueDate": {
      "type": "string",
      "format": "date-time",
      "description": "Target deadline in UTC ISO-8601"
    }
  }
}
```

### 2.2 `RegisterRequest` Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "RegisterRequest",
  "type": "object",
  "required": ["email", "password", "fullName"],
  "additionalProperties": false,
  "properties": {
    "email": {
      "type": "string",
      "format": "email",
      "maxLength": 255
    },
    "password": {
      "type": "string",
      "minLength": 8,
      "maxLength": 100,
      "pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    },
    "fullName": {
      "type": "string",
      "minLength": 1,
      "maxLength": 100
    }
  }
}
```

---

## 3. Core Response Schemas

### 3.1 `TaskResponse` Schema
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "TaskResponse",
  "type": "object",
  "required": ["id", "userId", "title", "status", "priority", "isOverdue", "createdAt", "updatedAt"],
  "properties": {
    "id": { "type": "string", "format": "uuid" },
    "userId": { "type": "string", "format": "uuid" },
    "title": { "type": "string" },
    "description": { "type": ["string", "null"] },
    "status": { "type": "string", "enum": ["TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"] },
    "priority": { "type": "string", "enum": ["LOW", "MEDIUM", "HIGH", "URGENT"] },
    "dueDate": { "type": ["string", "null"], "format": "date-time" },
    "isOverdue": { "type": "boolean" },
    "createdAt": { "type": "string", "format": "date-time" },
    "updatedAt": { "type": "string", "format": "date-time" }
  }
}
```

---

## 4. Review Checklist
- [x] JSON Schema Draft 2020-12 compliant schemas documented for all core operations.
- [x] String patterns, enum constraints, and date-time formats specified.
- [x] Strict `additionalProperties: false` ensures payload sanitization.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | API Architect | Initial baseline Request and Response Schemas. |

