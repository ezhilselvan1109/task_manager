# Workflow 01: New Microservice Development

## 1. Overview & Purpose
Defines the complete end-to-end procedure for initializing and scaffolding a new Spring Boot microservice within the Task Management System architecture.

- **Workflow ID**: `WF-001`
- **Trigger**: Initiation of a new microservice module (e.g. `user-service`, `task-service`, `notification-service`).

## 2. Preconditions
- Root parent `pom.xml` configured with Java 21 and Spring Boot 3.3.4 dependencies.
- Service boundary and database ownership documented in Low-Level Design (Phase 5).
- Service Discovery (`service-discovery`) and API Gateway (`api-gateway`) definitions updated.

## 3. Required Inputs
- Service module directory name.
- Logical database name (e.g. `task_db`).
- Port allocation (e.g. `8082`).
- Domain entity specifications and API requirements.

## 4. Execution Steps
1. **Module Scaffolding**:
   - Create Maven submodule with parent POM reference.
   - Configure dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-actuator`, `micrometer-registry-prometheus`, `flyway-core`, `postgresql`.
2. **Directory and Package Setup**:
   - Establish standard package structure: `com.taskmanager.<service>.{config, controller, service, repository, entity, dto, exception, mapper}`.
3. **Configuration Setup**:
   - Create `src/main/resources/application.yml` with Eureka registration, database URL, and Actuator endpoints.
4. **Flyway Migration Baseline**:
   - Add initial migration in `src/main/resources/db/migration/V1_0__initial_schema.sql`.
5. **Spring Application Entry Point**:
   - Create `@SpringBootApplication` main class.
6. **Health and Discovery Test**:
   - Write `@SpringBootTest` verifying context loads and registers with Eureka.

## 5. Skills to Use
- [`spring-boot-microservice`](file:///d:/learning/task_manager/.agents/skills/spring-boot-microservice/SKILL.md)
- [`java-21-development`](file:///d:/learning/task_manager/.agents/skills/java-21-development/SKILL.md)
- [`spring-cloud-api-gateway`](file:///d:/learning/task_manager/.agents/skills/spring-cloud-api-gateway/SKILL.md)
- [`flyway-database-migrations`](file:///d:/learning/task_manager/.agents/skills/flyway-database-migrations/SKILL.md)

## 6. Rules to Follow
- [Rule 01: Architecture & Stack](file:///d:/learning/task_manager/.agents/rules/01-architecture-and-stack.md)
- [Rule 02: Java 21 & Spring Boot](file:///d:/learning/task_manager/.agents/rules/02-java21-and-spring-boot.md)
- [Rule 03: Spring Cloud & Gateway](file:///d:/learning/task_manager/.agents/rules/03-spring-cloud-and-gateway.md)

## 7. Tests and Validation
- Execute `mvn clean test-compile` to verify module structure.
- Execute `mvn test` to verify ApplicationContext startup.
- Validate that `spring.jpa.hibernate.ddl-auto=validate`.

## 8. Failure Handling
- If Spring context fails to load, inspect `application.yml` for missing database connection properties.
- If Eureka registration fails, verify Eureka service discovery URL parameter.

## 9. Review Checklist
- [ ] Module registered in root `pom.xml` `<modules>`.
- [ ] Package naming follows `com.taskmanager.<service>`.
- [ ] Dockerfile created using multi-stage Alpine JRE 21 template.
- [ ] Service added to `docker-compose.yml` with memory limit `384m`.

## 10. Expected Outputs
- Fully compilable microservice submodule.
- Initial Flyway migration script.
- Context load test passing.

