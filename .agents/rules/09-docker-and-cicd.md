# Rule 09: Docker & CI/CD Standards

## Docker & Containerization Rules
1. **Base Images**:
   - Backend services must use `eclipse-temurin:21-jre-alpine` for production images.
   - Frontend must use multi-stage build: `node:20-alpine` build stage $\rightarrow$ `nginx:1.25-alpine` runtime stage.
2. **Layered JAR Optimization**:
   - Extract Spring Boot layered JAR (`java -Djarmode=layertools -jar app.jar extract`) into `dependencies`, `spring-boot-loader`, `snapshot-dependencies`, and `application` layers to maximize Docker cache reusability.
3. **Container Security**:
   - Containers must run as an unprivileged user (`adduser -S appuser`).
   - Never run containers as root.
4. **Memory Limits & 5.25 GB Budget**:
   - Microservices: `mem_limit: 384m`, `-Xms128m -Xmx256m`.
   - PostgreSQL: `mem_limit: 512m`.
   - Kafka: `mem_limit: 768m`, `-Xms256m -Xmx512m`.
   - Prometheus: `mem_limit: 256m`.
   - Grafana: `mem_limit: 128m`.
   - All 11 containers combined must not exceed 5.25 GB memory allocation.

## CI/CD Pipeline Standards
1. **Declarative Jenkins Pipeline**:
   - Managed via root `Jenkinsfile` executing 7 sequential stages:
     1. Checkout SCM
     2. Static Code Analysis & Linting (Checkstyle / SpotBugs)
     3. Backend Build & Unit Tests (`mvn clean test`)
     4. Integration Tests (`mvn verify -Pintegration-tests`)
     5. Docker Image Build & Layer Verification
     6. Container Vulnerability Scanning (Trivy)
     7. Deployment & Health Verification (`/actuator/health`)
2. **Fast-Failure**:
   - Pipeline must immediately abort upon test failure, compiler warning, or critical security vulnerability.

