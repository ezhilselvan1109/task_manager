# Container Configuration Specification

**Document Identifier:** DOC-INF-006  
**Traceability References:** NFR-007, DOC-INF-005  
**Target Services:** All 11 Containerized Services  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the container configuration standards, JVM garbage collection tuning, health check thresholds, and graceful shutdown parameters for all Docker containers in the Task Management system.

---

## 2. Container Configuration Inventory

| Container Name | Base Image | Memory Limit | JVM Heap (-Xms / -Xmx) | GC Algorithm | Health Check Endpoint | Stop Grace Period |
|---|---|---|---|---|---|---|
| `taskmanager-discovery` | `eclipse-temurin:21-jre-alpine` | 256 MB | `-Xms64m -Xmx192m` | SerialGC | `/actuator/health` | 15s |
| `taskmanager-postgresql`| `postgres:16-alpine` | 512 MB | N/A (shared_buffers 128M) | N/A | `pg_isready -U postgres` | 30s |
| `taskmanager-zookeeper` | `cp-zookeeper:7.6.0` | 256 MB | `-Xms64m -Xmx128m` | G1GC | Port check 2181 | 15s |
| `taskmanager-kafka` | `cp-kafka:7.6.0` | 768 MB | `-Xms256m -Xmx512m` | G1GC | Port check 9092 | 30s |
| `taskmanager-api-gateway`| `eclipse-temurin:21-jre-alpine`| 384 MB | `-Xms128m -Xmx256m` | G1GC | `/actuator/health` | 30s |
| `taskmanager-user-service`| `eclipse-temurin:21-jre-alpine`| 384 MB | `-Xms128m -Xmx256m` | G1GC | `/actuator/health` | 30s |
| `taskmanager-task-service`| `eclipse-temurin:21-jre-alpine`| 512 MB | `-Xms128m -Xmx384m` | G1GC | `/actuator/health` | 30s |
| `taskmanager-notification`| `eclipse-temurin:21-jre-alpine`| 384 MB | `-Xms128m -Xmx256m` | G1GC | `/actuator/health` | 30s |
| `taskmanager-frontend` | `nginx:1.25-alpine` | 128 MB | N/A | N/A | N/A (Port 80 TCP) | 10s |
| `taskmanager-prometheus`| `prom/prometheus:v2.51.0` | 256 MB | N/A | N/A | `/-/healthy` | 15s |
| `taskmanager-grafana` | `grafana/grafana:10.4.0` | 256 MB | N/A | N/A | `/api/health` | 15s |

---

## 3. JVM Container Tuning Specifications

For small containers ($\le 256$ MB, e.g. `service-discovery`), the Serial GC is mandated to eliminate the memory footprint of concurrent G1GC worker threads:
```properties
JAVA_OPTS="-XX:+UseSerialGC -XX:+UseContainerSupport -Xms64m -Xmx192m -XX:+ExitOnOutOfMemoryError"
```

For application services (`api-gateway`, `user-service`, `task-service`, `notification-service`), G1GC is configured:
```properties
JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError -XX:MaxGCPauseMillis=100"
```

- `-XX:+ExitOnOutOfMemoryError`: Guarantees that if a memory leak occurs, the JVM exits immediately with a nonzero code, triggering Docker's automated container restart rather than hanging in an unresponsive state.

---

## 4. Graceful Shutdown Configuration

All Spring Boot microservices enable graceful shutdown in `application.yml`:
```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 25s
```
When Docker issues `SIGTERM`:
1. API Gateway stops accepting new connections and finishes in-flight requests.
2. Microservices complete ongoing HTTP responses and database commits.
3. Kafka consumers complete current batch offsets and gracefully leave the consumer group.
4. If work is not completed within 25 seconds, the container is forcibly terminated (`SIGKILL`).

