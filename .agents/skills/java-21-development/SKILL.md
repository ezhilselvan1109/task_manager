---
name: java-21-development
description: >-
  Use this skill when developing or refactoring Java 21 backend code, utilizing modern language
  features such as records, pattern matching, sealed interfaces, and text blocks according to project standards.
---

# Java 21 Development Skill

## Purpose
Provides standardized guidelines, patterns, and procedures for authoring idiomatic, performant, and type-safe Java 21 code within the Task Management System microservices.

## When to Use
- Implementing new domain models, DTOs, or service components in Java 21.
- Modernizing or refactoring legacy Java idioms (e.g. replacing boilerplate classes with records).
- Implementing pattern matching in business rule evaluations and exception decoders.

## Required Inputs
- Target microservice module path (`user-service`, `task-service`, `notification-service`, `api-gateway`, or `service-discovery`).
- Feature specification or requirement ID (e.g. `FR-007`).

## Step-by-Step Procedure
1. **Verify Toolchain**: Ensure Maven compiler plugin targets bytecode version 21:
   ```xml
   <properties>
       <java.version>21</java.version>
       <maven.compiler.source>21</maven.compiler.source>
       <maven.compiler.target>21</maven.compiler.target>
   </properties>
   ```
2. **Utilize Records for Immutable Data Structures**:
   - DTOs, Kafka payloads, and API response envelopes must be declared as Java records:
     ```java
     public record TaskResponse(
         UUID id,
         UUID userId,
         String title,
         String description,
         TaskStatus status,
         TaskPriority priority,
         Instant dueDate,
         Instant createdAt,
         Instant updatedAt
     ) {}
     ```
3. **Apply Pattern Matching for Switch**:
   - Use exhaustive pattern matching for status transitions and event types:
     ```java
     public String resolveEventSummary(TaskEvent event) {
         return switch (event) {
             case TaskCreatedEvent created -> "Task created: " + created.title();
             case TaskUpdatedEvent updated -> "Task updated: " + updated.taskId();
             case TaskCompletedEvent completed -> "Task marked completed";
             case TaskDeletedEvent deleted -> "Task deleted";
         };
     }
     ```
4. **Use Text Blocks for Multi-Line Strings**:
   - Use `"""` for complex SQL queries, JSON test fixtures, and HTML templates.
5. **Enforce Immutability & Null-Safety**:
   - Use `java.util.Optional<T>` exclusively for query return types that may be empty.
   - Collections must default to empty immutable collections (`List.of()`, `Collections.emptyList()`).

## Applicable Project Standards
- [Rule 02: Java 21 & Spring Boot Standards](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Backend Development Guide](file:///d:/learning/task_manager/documentation/12-development/01-backend-development-guide.md)

## Validation Checklist
- [ ] Code compiles cleanly under JDK 21 (`javac --release 21`).
- [ ] Immutable objects use `record` instead of Lombok `@Data` or manual getters/setters.
- [ ] No `Optional` fields or method parameters.
- [ ] Pattern matching handles all enum/record cases exhaustively.

## Common Mistakes
- Using mutable Java classes with Lombok `@Data` for API DTOs.
- Returning `null` instead of `Optional.empty()` or an empty collection.
- Calling `Optional.get()` without checking `isPresent()`.

## Completion Criteria
Code compiles with zero warnings, adheres to Java 21 syntax, and all associated unit tests pass.

