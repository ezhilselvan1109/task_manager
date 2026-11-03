# Workflow 12: Docker Build and Validation

## 1. Overview & Purpose
Defines the procedure for building container images, verifying multi-stage layering, starting the Docker Compose mesh, and validating container health and memory consumption.

- **Workflow ID**: `WF-012`
- **Trigger**: New service packaging, Dockerfile modification, or local stack integration validation.

## 2. Preconditions
- Docker engine and Docker Compose installed and running.
- Maven backend packages compiled (`mvn clean package -DskipTests`).

## 3. Required Inputs
- Target Dockerfile or root `docker-compose.yml`.
- Target service container name.

## 4. Execution Steps
1. **Build Container Image**:
   - Execute: `docker compose build <service-name>`.
   - Verify multi-stage extraction creates minimal image layers.
2. **Start Service in Isolation or Stack**:
   - Run: `docker compose up -d <service-name>`.
3. **Inspect Container Health**:
   - Run: `docker compose ps`.
   - Verify container status transitions to `healthy` via Actuator `/actuator/health`.
4. **Inspect Container Resource Usage**:
   - Run: `docker stats --no-stream`.
   - Confirm RAM consumption is within specified limits (`mem_limit`).
5. **Inspect Startup Logs**:
   - Run: `docker compose logs --tail=100 <service-name>`.
   - Verify clean Spring Boot banner and absence of fatal exceptions or stack traces.

## 5. Skills to Use
- [`docker-docker-compose`](file:///d:/learning/task_manager/.agents/skills/docker-docker-compose/SKILL.md)
- [`spring-boot-microservice`](file:///d:/learning/task_manager/.agents/skills/spring-boot-microservice/SKILL.md)

## 6. Rules to Follow
- [Rule 09: Docker & CI/CD Standards](file:///d:/learning/task_manager/.agents/rules/09-docker-and-cicd.md)
- [Rule 10: Observability & Operational Standards](file:///d:/learning/task_manager/.agents/rules/10-observability-and-ops.md)

## 7. Tests and Validation
- Container healthcheck returns exit code 0 (`curl -f http://localhost:<port>/actuator/health`).
- Container runs as non-root user (`appuser`).

## 8. Failure Handling
- If container exits with code 137 (OOMKilled), increase container `mem_limit` or tune `-Xmx` down.
- If database connection refused, ensure PostgreSQL container is up and healthy.

## 9. Review Checklist
- [ ] Container runs as non-root user.
- [ ] Layered JAR optimization is utilized.
- [ ] Memory limit is enforced in `docker-compose.yml`.
- [ ] Actuator healthcheck passes.

## 10. Expected Outputs
- Validated Docker image.
- Running healthy container.
- Verification log showing clean startup.

