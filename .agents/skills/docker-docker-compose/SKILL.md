---
name: docker-docker-compose
description: >-
  Use this skill when authoring Dockerfiles, configuring docker-compose.yml, managing container
  networks, environment variables, healthchecks, and memory limits.
---

# Docker & Docker Compose Skill

## Purpose
Governs the creation of production-grade multi-stage Dockerfiles and the maintenance of the root `docker-compose.yml` local orchestration mesh within the strict 5.25 GB RAM budget.

## When to Use
- Authoring or modifying Dockerfiles for microservices or the frontend.
- Adding, updating, or tuning services in `docker-compose.yml`.
- Starting, stopping, and verifying the multi-container environment.
- Troubleshooting container networking, port mapping, and health checks.

## Required Inputs
- Root `docker-compose.yml`.
- Service module paths and memory constraints.

## Step-by-Step Procedure
1. **Author Multi-Stage Spring Boot Dockerfile**:
   In `<service>/Dockerfile`:
   ```dockerfile
   # Stage 1: Build Layer Extractor
   FROM eclipse-temurin:21-jre-alpine AS builder
   WORKDIR /application
   ARG JAR_FILE=target/*.jar
   COPY ${JAR_FILE} application.jar
   RUN java -Djarmode=layertools -jar application.jar extract

   # Stage 2: Runtime Container
   FROM eclipse-temurin:21-jre-alpine
   WORKDIR /application
   RUN addgroup -S appgroup && adduser -S appuser -G appgroup
   USER appuser:appgroup
   COPY --from=builder /application/dependencies/ ./
   COPY --from=builder /application/spring-boot-loader/ ./
   COPY --from=builder /application/snapshot-dependencies/ ./
   COPY --from=builder /application/application/ ./
   ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
   ```
2. **Configure Docker Compose Services**:
   - Enforce memory caps (`mem_limit: 384m`) and health checks:
     ```yaml
     task-service:
       build: ./task-service
       container_name: task-service
       environment:
         - SPRING_PROFILES_ACTIVE=docker
         - JAVA_TOOL_OPTIONS=-Xms128m -Xmx256m
       mem_limit: 384m
       depends_on:
         postgres:
           condition: service_healthy
         kafka:
           condition: service_healthy
         service-discovery:
           condition: service_healthy
     ```
3. **Manage Lifecycle Commands**:
   - Start entire mesh: `docker compose up -d`
   - View container status: `docker compose ps`
   - Inspect service logs: `docker compose logs -f <service-name>`
   - Stop mesh: `docker compose down`

## Applicable Project Standards
- [Rule 09: Docker & CI/CD Standards](file:///d:/learning/task_manager/.agents/rules/09-docker-and-cicd.md)
- [Docker Compose Specification](file:///d:/learning/task_manager/documentation/11-infrastructure/04-docker-compose-specification.md)

## Validation Checklist
- [ ] Non-root execution (`USER appuser`).
- [ ] Memory limits defined on all 11 containers ($\le 5.25\text{ GB}$ total).
- [ ] Health checks configured with appropriate start periods and retries.
- [ ] `depends_on` conditions enforce ordered container initialization.

## Common Mistakes
- Running containers as `root`.
- Forgetting memory limits, causing host OOM lockups.
- Copying the monolithic fat JAR directly into Docker without layer extraction.

## Completion Criteria
All containers build successfully, start in healthy status, and operate comfortably within the 5.25 GB host RAM budget.

