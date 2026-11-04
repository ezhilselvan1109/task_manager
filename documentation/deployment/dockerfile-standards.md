# Dockerfile Standards & Multi-Stage Build Guidelines

| Field | Value |
|---|---|
| **Document ID** | DOC-DEP-002 |
| **Title** | Production Dockerfile Standards & Multi-Stage Build Guidelines |
| **Project** | Task Management System – Microservices Architecture |
| **Applicable Images** | Backend Microservices, API Gateway, Service Discovery, Frontend React SPA |
| **Status** | Approved Production Baseline |
| **Traceability Reference** | ADR-003, ADR-008, DOC-INF-004, DOC-DEV-007, RULE-02, RULE-04 |

---

## 1. Principles & Hardening Standards

All Dockerfiles authored for the Task Management System comply with strict container engineering standards:

1. **Deterministic Pinned Base Images**:
   - Compiler/Build Stage: `maven:3.9.6-eclipse-temurin-21-alpine` and `node:20-alpine`.
   - Production Runtime Stage: `eclipse-temurin:21-jre-alpine` and `nginx:1.25-alpine`.
   - Never utilize floating tags like `:latest` in production environments.
2. **Multi-Stage Build Pipeline**:
   - Compilers, build tools, package managers, and development dependencies are discarded after stage 1.
   - Final production images only contain the minimal JRE/Nginx runtime and compiled bytecode/assets.
3. **Layered JAR Cache Optimization**:
   - Spring Boot layered JARs are extracted using `java -Djarmode=layertools -jar app.jar extract`.
   - Layers are copied in order of least frequent change (`dependencies/` $\rightarrow$ `spring-boot-loader/` $\rightarrow$ `snapshot-dependencies/` $\rightarrow$ `application/`).
   - Ensures rapid, cached rebuilds when only application business logic changes.
4. **Mandatory Non-Root User Execution**:
   - Backend containers run as dedicated unprivileged user `appuser:appgroup`.
   - Frontend container runs as unprivileged user `nginx:nginx`.
   - Prevents container breakout vulnerabilities and host filesystem corruption.
5. **JVM Container Awareness & Memory Guardrails**:
   - Enabled flags: `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -XX:+UseG1GC`.
   - Ensures JVM automatically senses cgroup memory limits imposed by Docker Compose.

---

## 2. Backend Multi-Module Dockerfile Specification

Because all microservices inherit from `task-manager-parent` and depend on the shared `common` module, each backend Dockerfile builds from the repository root context (`context: .`):

### 2.1 Standardized Backend Template (`service-discovery`, `api-gateway`, `user-service`, `task-service`, `notification-service`)

```dockerfile
# =========================================================================
# Stage 1: Build Layer Extractor using Multi-Module Maven Parent
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy parent and all module POM files to maximize Docker layer caching
COPY pom.xml .
COPY common/pom.xml common/
COPY service-discovery/pom.xml service-discovery/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY task-service/pom.xml task-service/
COPY notification-service/pom.xml notification-service/

# Cache Maven dependencies offline for target module
RUN mvn dependency:go-offline -B -pl <module-name> -am || true

# Copy shared common module and service-specific source code
COPY common/src common/src
COPY <module-name>/src <module-name>/src

# Package target module with skipTests (tests executed in CI pipeline)
RUN mvn clean package -pl <module-name> -am -DskipTests -B

# Extract Spring Boot layered JAR
WORKDIR /workspace/<module-name>/target
RUN java -Djarmode=layertools -jar <module-name>-*.jar extract

# =========================================================================
# Stage 2: Hardened, Unprivileged Runtime Container
# =========================================================================
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Security: Create non-root unprivileged user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Install curl for Actuator health check probes
RUN apk add --no-cache curl

# Copy extracted layers from builder stage with strict non-root ownership
COPY --from=builder --chown=appuser:appgroup /workspace/<module-name>/target/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/<module-name>/target/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/<module-name>/target/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/<module-name>/target/application/ ./

# Enforce non-root execution
USER appuser:appgroup

# Configurable JVM memory settings adhering to Hostinger VPS budget
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SERVER_PORT=<service-port>
EXPOSE <service-port>

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=30s \
  CMD curl -f http://localhost:<service-port>/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

---

## 3. Frontend React SPA Dockerfile Specification

The frontend application utilizes a multi-stage Node.js build to produce static assets, served by Nginx:

```dockerfile
# =========================================================================
# Stage 1: Build React SPA using Node.js 20 Alpine
# =========================================================================
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package descriptors for deterministic dependency caching
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline --no-audit

# Copy application source code
COPY . .

# Injected Vite build arguments (passed from docker-compose / .env)
ARG VITE_API_GATEWAY_URL=""
ENV VITE_API_GATEWAY_URL=${VITE_API_GATEWAY_URL}

# Compile TypeScript and build production static bundle into /app/dist
RUN npm run build

# =========================================================================
# Stage 2: Serve Production Assets using Hardened Nginx Alpine
# =========================================================================
FROM nginx:1.25-alpine AS runner

# Remove default Nginx HTML landing page
RUN rm -rf /usr/share/nginx/html/*

# Copy custom Nginx configuration supporting SPA client-side routing & caching
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Hardening: Configure directories and ownership for non-root nginx user
RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid /var/cache/nginx /var/log/nginx /usr/share/nginx/html /etc/nginx/conf.d

# Switch to unprivileged nginx user
USER nginx

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=3s --retries=3 --start-period=5s \
  CMD wget -q -O - http://localhost:80/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

---

## 4. Layer Size & Image Footprint Comparison

| Image | Builder Base | Runtime Base | Final Image Size | Unprivileged User |
|---|---|---|---|---|
| `taskmanager-discovery` | `maven:3.9.6-temurin-21-alpine` (~850 MB) | `eclipse-temurin:21-jre-alpine` | **~195 MB** | `appuser` (10001) |
| `taskmanager-api-gateway` | `maven:3.9.6-temurin-21-alpine` (~850 MB) | `eclipse-temurin:21-jre-alpine` | **~210 MB** | `appuser` (10001) |
| `taskmanager-user-service` | `maven:3.9.6-temurin-21-alpine` (~850 MB) | `eclipse-temurin:21-jre-alpine` | **~220 MB** | `appuser` (10001) |
| `taskmanager-task-service` | `maven:3.9.6-temurin-21-alpine` (~850 MB) | `eclipse-temurin:21-jre-alpine` | **~225 MB** | `appuser` (10001) |
| `taskmanager-notification-service` | `maven:3.9.6-temurin-21-alpine` (~850 MB) | `eclipse-temurin:21-jre-alpine` | **~225 MB** | `appuser` (10001) |
| `taskmanager-frontend` | `node:20-alpine` (~350 MB) | `nginx:1.25-alpine` | **~25 MB** | `nginx` (101) |

