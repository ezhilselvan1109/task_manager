# Input Validation and Data Protection

**Document Identifier:** DOC-SEC-008  
**Traceability References:** SEC-007, SEC-008, NFR-003, FR-001 to FR-010, BRULE-003  
**Target Services:** All Platform Components  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the platform-wide data validation architecture and defensive programming controls that protect the Task Management System from injection attacks, corrupted state, buffer overflow exploits, and unauthorized data leakage.

---

## 2. Five-Tier Validation Defense Model

Data integrity is guarded through five successive defense perimeters:

```
[ User Input in Web Browser ]
             │
             ▼
┌────────────────────────────────────────┐
│ Tier 1: Client-Side Validation         │
│ React Hook Form + Zod schema           │
│ Immediate interactive UX feedback      │
└────────────────────┬───────────────────┘
                     │
                     ▼
┌────────────────────────────────────────┐
│ Tier 2: Gateway Ingress Validation     │
│ Content-Type: application/json         │
│ Payload size <= 1 MB                   │
└────────────────────┬───────────────────┘
                     │
                     ▼
┌────────────────────────────────────────┐
│ Tier 3: Controller DTO Validation      │
│ Jakarta Bean Validation (@Valid)       │
│ JSR-380 constraints (@NotBlank, @Size) │
└────────────────────┬───────────────────┘
                     │
                     ▼
┌────────────────────────────────────────┐
│ Tier 4: Service Domain Validation      │
│ State transition matrix verification   │
│ Task ownership and due-date logic      │
└────────────────────┬───────────────────┘
                     │
                     ▼
┌────────────────────────────────────────┐
│ Tier 5: Persistence Schema Constraints │
│ PostgreSQL NOT NULL, CHECK, UNIQUE     │
│ Column size limits (VARCHAR(255))      │
└────────────────────────────────────────┘
```

---

## 3. Specific Injection Defenses

### 3.1 SQL Injection (SQLi) Defense
- **Parameterized Queries**: All queries execute exclusively via Spring Data JPA method abstractions or JPA Criteria API specifications. Parameter substitution uses prepared statement placeholders (`?` or `:paramName`).
- **Zero Raw String Concatenation**: Code reviews strictly prohibit any dynamic JPQL or native SQL string concatenation (e.g. `"SELECT * FROM tasks WHERE title = '" + input + "'"` is banned).
- **Sort Parameter Whitelist**: Dynamic sort column parameters are validated against a strict compile-time whitelist (`createdAt`, `dueDate`, `priority`, `status`, `title`). Any unlisted sort key is rejected with `HTTP 400 Bad Request`.

### 3.2 Cross-Site Scripting (XSS) Defense
- **React Contextual Encoding**: The React frontend uses JSX syntax, which automatically escapes all string expressions prior to rendering into the DOM, neutralizing stored and reflected script injection.
- **HTML Character Neutralization**: Free-form text fields (`task.description`, `task.title`) are stored as raw UTF-8 text; HTML markup is neither interpreted nor executed by the client application.

### 3.3 Path Traversal and Command Injection
- Microservices execute zero operating system shell commands (`Runtime.getRuntime().exec` is strictly prohibited).
- Microservices never map user input to local filesystem file paths; all entity identifiers use immutable `UUID v4` values.

---

## 4. DTO Validation Constraints Inventory

| DTO Field | Target Field | Jakarta Constraints | Description |
|---|---|---|---|
| `RegisterRequest` | `username` | `@NotBlank, @Size(min = 3, max = 50), @Pattern(regexp = "^[a-zA-Z0-9_.-]+$")` | Alphanumeric characters with dots, underscores, hyphens. |
| `RegisterRequest` | `email` | `@NotBlank, @Email, @Size(max = 100)` | Valid RFC 5322 email format. |
| `RegisterRequest` | `password` | `@NotBlank, @Pattern(regexp = "^(?=.*[a-z])...$")` | Strong password complexity rules. |
| `CreateTaskRequest`| `title` | `@NotBlank, @Size(min = 1, max = 255)` | Non-empty task title. |
| `CreateTaskRequest`| `description`| `@Size(max = 2000)` | Optional description capped at 2,000 characters. |
| `CreateTaskRequest`| `status` | `@NotNull, @ValidEnum(enumClass = TaskStatus.class)` | Must match TODO, IN_PROGRESS, COMPLETED, CANCELLED. |
| `CreateTaskRequest`| `priority` | `@NotNull, @ValidEnum(enumClass = TaskPriority.class)` | Must match LOW, MEDIUM, HIGH, URGENT. |
| `CreateTaskRequest`| `dueDate` | Optional ISO-8601 UTC Instant | Must be syntactically valid timestamp string. |

---

## 5. Data Privacy and Sensitive Data Protection

1. **PII Masking**: Email addresses logged in operational log statements are sanitized: `j***e@example.com`.
2. **Zero Sensitive Payload Logging**: HTTP request bodies for authentication endpoints (`/login`, `/register`) are explicitly excluded from debug and access logging.
3. **Soft-Delete Anonymization & Data Isolation**: Deleted entities have `is_deleted = true` set. Queries enforce `AND is_deleted = false`, ensuring soft-deleted user and task data is immediately hidden from application responses.

