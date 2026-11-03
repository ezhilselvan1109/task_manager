# Coding Standards and Style Guide

**Document Identifier:** DOC-AUTO-010  
**Traceability References:** Master Project Baseline, DOC-AUTO-001, DOC-AUTO-002  
**Languages & Frameworks:** Java 21, Spring Boot 3.3.x, React 18, TypeScript 5.x  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document establishes the mandatory coding standards, style guidelines, and idiomatic best practices for backend (Java 21 / Spring Boot) and frontend (React 18 / TypeScript) development across the Task Management System.

---

## 2. Backend Coding Standards (Java 21 & Spring Boot)

### 2.1 Modern Java 21 Idiomatic Patterns
- **DTOs & Value Objects**: Always declare DTOs, query criteria, and event envelopes as Java 21 `record` types to ensure immutability and eliminate boilerplate.
  ```java
  public record CreateTaskRequest(
      @NotBlank(message = "Task title is mandatory")
      @Size(max = 255, message = "Title cannot exceed 255 characters")
      String title,

      @Size(max = 2000, message = "Description cannot exceed 2000 characters")
      String description,

      @NotNull(message = "Priority is mandatory")
      TaskPriority priority,

      Instant dueDate
  ) {}
  ```
- **Pattern Matching**: Use pattern matching for `instanceof` and modern enhanced `switch` expressions:
  ```java
  return switch (taskStatus) {
      case TODO -> "Task is pending execution";
      case IN_PROGRESS -> "Task is currently active";
      case COMPLETED -> "Task finished successfully";
      case CANCELLED -> "Task was abandoned";
  };
  ```
- **Text Blocks**: Use multi-line text blocks (`"""`) for complex Flyway DDL scripts or multiline JSON templates.

### 2.2 Spring Boot Architecture Practices
- **Constructor Injection**: Never use field-based injection (`@Autowired private TaskRepository repo;`). Always use constructor injection, preferably via Lombok `@RequiredArgsConstructor(onConstructor_ = @Autowired)` or explicit constructor.
- **Transactional Discipline**:
  - Keep `@Transactional` on the service method level.
  - Set `@Transactional(readOnly = true)` for read/query methods to allow Hibernate query optimizations and prevent dirty checking overhead.
  - Never wrap external HTTP calls, remote Feign clients, or Kafka publishing calls inside a database transaction.
- **Logging Standards**:
  - Use Slf4j with parameter placeholders (never concatenate strings in log statements):
    ```java
    // CORRECT:
    log.info("Task created successfully with id: {} for user: {}", task.getId(), userId);

    // BANNED:
    log.info("Task created " + task.getId() + " user " + userId);
    ```

---

## 3. Frontend Coding Standards (React 18 & TypeScript)

### 3.1 TypeScript Strictness
- `tsconfig.json` enforces:
  ```json
  {
    "compilerOptions": {
      "strict": true,
      "noImplicitAny": true,
      "strictNullChecks": true,
      "noUnusedLocals": true,
      "noUnusedParameters": true
    }
  }
  ```
- Use of the `any` type is strictly prohibited. Use `unknown` with type narrowing guards when data types are indeterminate.

### 3.2 Functional Components and Hooks
- All UI components are functional components with strict TypeScript prop contracts:
  ```tsx
  interface TaskCardProps {
    readonly task: Task;
    readonly onStatusChange: (taskId: string, newStatus: TaskStatus) => Promise<void>;
  }

  export const TaskCard: React.FC<TaskCardProps> = ({ task, onStatusChange }) => {
    // Component logic
  };
  ```
- Class-based React components are prohibited.

### 3.3 Styling and State Architecture
- **TailwindCSS**: All layout and presentation logic uses utility-first Tailwind classes. Hardcoded CSS styles or inline `style={{ ... }}` objects are prohibited except for dynamic coordinates.
- **Server State**: Managed via TanStack Query (React Query) for caching, refetching, and optimistic updates.
- **Client UI State**: Managed via Zustand or React Context for modal dialogs and notification trays.

