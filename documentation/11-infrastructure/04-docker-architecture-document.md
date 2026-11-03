# Docker Architecture Document

**Document Identifier:** DOC-INF-004  
**Traceability References:** NFR-007, ADR-003  
**Target Environments:** Container Builds, Local Docker Compose, CI/CD Packaging  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the Docker containerization architecture, multi-stage image build patterns, layer caching strategies, and security hardening controls for all Task Management microservices and frontend applications.

---

## 2. Multi-Stage Build Architecture

All Spring Boot microservices utilize standardized multi-stage Dockerfiles to minimize final image footprint and eliminate build tools from runtime containers.

```
┌────────────────────────────────────────────────────────┐
│ Stage 1: Builder (maven:3.9.6-eclipse-temurin-21-alpine)│
│ - Copy pom.xml & download Maven dependencies           │
│ - Copy src/ & execute mvn clean package -DskipTests    │
│ - Produce compiled, executable Spring Boot fat JAR     │
└───────────────────────────┬────────────────────────────┘
                            │
              Extract Jar / Layers
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 2: Runtime (eclipse-temurin:21-jre-alpine)       │
│ - Create unprivileged user (appuser: 10001)            │
│ - Copy application JAR from Builder stage              │
│ - Apply JVM Container Flags: -Xms128m -Xmx256m         │
│ - Final Image Size: ~180 MB (down from 850 MB)         │
└────────────────────────────────────────────────────────┘
```

---

## 3. Reference Spring Boot Multi-Stage Dockerfile

```dockerfile
# =========================================================================
# STAGE 1: Dependency Caching & Compilation
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile
COPY src ./src
RUN mvn clean package -DskipTests -B

# =========================================================================
# STAGE 2: Hardened, Unprivileged Runtime Container
# =========================================================================
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Security: Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Install curl for Actuator health checks
RUN apk add --no-cache curl

# Copy fat JAR from build stage
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

# Run as non-root user
USER appuser

# JVM Container awareness and memory limits
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 4. Frontend React Multi-Stage Dockerfile

```dockerfile
# =========================================================================
# STAGE 1: Vite React Build
# =========================================================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# =========================================================================
# STAGE 2: Nginx Web Server
# =========================================================================
FROM nginx:1.25-alpine AS frontend-runner

# Copy compiled static SPA assets
COPY --from=frontend-builder /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration supporting SPA client-side routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 5. Security Hardening Controls
1. **Zero Root Execution**: Containers execute exclusively under unprivileged user `appuser` (UID `10001`), preventing container escape vulnerabilities.
2. **Alpine Base**: Utilizes ultra-lightweight Alpine Linux distributions, significantly reducing CVE exposure.
3. **No Embedded Build Tools**: Compilers, Maven packages, and source code are discarded after stage 1.

