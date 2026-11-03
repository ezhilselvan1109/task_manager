---
name: spring-boot-microservice
description: >-
  Use this skill when scaffolding, implementing, or updating Spring Boot 3.3.x microservices,
  including controller, service, repository, and configuration layers.
---

# Spring Boot Microservice Development Skill

## Purpose
Establishes the step-by-step procedure for implementing clean, layered microservices using Spring Boot 3.3.4, ensuring uniform package structures, constructor dependency injection, transaction management, and validation.

## When to Use
- Adding a new microservice or new service capabilities to `user-service`, `task-service`, or `notification-service`.
- Implementing business services, transactional boundaries, and configuration beans.

## Required Inputs
- Target module directory.
- Functional requirement ID (e.g., `FR-001`, `FR-007`).
- Entity/DTO models defined in Low-Level Design (Phase 5).

## Step-by-Step Procedure
1. **Define Package Hierarchy**:
   Ensure components reside in standard packages:
   - `com.taskmanager.<service>.config`
   - `com.taskmanager.<service>.controller`
   - `com.taskmanager.<service>.service`
   - `com.taskmanager.<service>.repository`
   - `com.taskmanager.<service>.entity`
   - `com.taskmanager.<service>.dto`
   - `com.taskmanager.<service>.exception`
2. **Apply Constructor Injection**:
   Use `@RequiredArgsConstructor` on classes with `private final` dependencies:
   ```java
   @Service
   @RequiredArgsConstructor
   public class TaskServiceImpl implements TaskService {
       private final TaskRepository taskRepository;
       private final TaskOutboxRepository outboxRepository;
       private final TaskMapper taskMapper;
       ...
   }
   ```
3. **Declare Explicit Transactional Boundaries**:
   - Annotate class with `@Transactional(readOnly = true)`.
   - Override state-mutating methods with `@Transactional`.
4. **Implement Service Interface and Implementation**:
   - Keep interfaces pure; implement business validation inside the service implementation before persisting.
5. **Configure Application Properties**:
   - Store configurations in `src/main/resources/application.yml`.
   - Parameterize connection strings with environment variables (e.g. `${SPRING_DATASOURCE_URL}`).

## Applicable Project Standards
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)
- [Rule 02: Java 21 & Spring Boot](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Phase 12 Service Blueprints](file:///d:/learning/task_manager/documentation/12-development/03-user-service-implementation-guide.md)

## Validation Checklist
- [ ] No field injection (`@Autowired` on private fields is forbidden).
- [ ] Mutating service methods are explicitly `@Transactional`.
- [ ] Configuration properties are validated using `@ConfigurationProperties` and `@Validated`.
- [ ] Layered separation of concerns is strictly preserved.

## Common Mistakes
- Omitting `@Transactional` on methods that perform database updates or outbox persistence.
- Putting business logic directly inside REST controllers.
- Hardcoding database or service discovery URLs in `application.yml`.

## Completion Criteria
Component compiles, injects dependencies cleanly in Spring ApplicationContext tests, and executes unit tests successfully.

