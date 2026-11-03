# Timeout Strategy Document

**Document Identifier:** DOC-RES-002  
**Traceability References:** NFR-002, NFR-006, DOC-RES-001  
**Target Services:** All Platform Components  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document defines the comprehensive timeout architecture across all network layers, database connections, inter-service calls, and asynchronous message streams. Strict timeout limits prevent thread starvation, connection pool exhaustion, and indefinite hangs during downstream degradation.

---

## 2. Timeout Architecture Hierarchy

```
[ Frontend Client: Timeout = 8s ]
               │
               ▼
[ API Gateway (Netty): Connect = 2s, Response = 5s ]
               │
               ▼
[ Spring Microservice Application Execution ]
        ├── JPA / Hibernate Query Timeout: 5s
        └── HikariCP Connection Acquisition: 3s
               │
               ▼
[ PostgreSQL Datastore: statement_timeout = 6s ]
```

---

## 3. Detailed Timeout Inventory

### 3.1 API Gateway HTTP Ingress & Routing Timeouts
Configured in `api-gateway/src/main/resources/application.yml`:
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 2000          # 2 seconds connection timeout
        response-timeout: 5s           # 5 seconds read response timeout
        pool:
          type: ELASTIC
          max-idle-time: 15s
```
- **Rationale**: If a downstream service does not establish a TCP connection within 2 seconds or return data within 5 seconds, the Gateway terminates the connection and returns `HTTP 504 Gateway Timeout`.

### 3.2 Database Connection Pool (HikariCP) Timeouts
Configured across all microservice `application.yml` files:
```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 3000        # 3 seconds max wait to acquire connection
      idle-timeout: 600000            # 10 minutes max idle before retirement
      max-lifetime: 1800000           # 30 minutes max connection lifespan
      leak-detection-threshold: 5000  # 5 seconds: logs warning if connection held
```
- **Rationale**: If all connections in the pool are busy, callers wait at most 3 seconds before failing fast. The leak detection threshold identifies slow or unclosed transactions during testing.

### 3.3 JPA & Database Statement Timeouts
```properties
# Spring Data JPA / Hibernate statement timeout
spring.jpa.properties.javax.persistence.query.timeout=5000
```
- **Rationale**: Prevents unindexed or runaway queries from locking database tables indefinitely. In PostgreSQL, `statement_timeout = '6s'` acts as the ultimate backend safety ceiling.

### 3.4 Apache Kafka Messaging Timeouts
```yaml
spring:
  kafka:
    producer:
      properties:
        max.block.ms: 5000            # Block for max 5s if metadata/buffer unavailable
        request.timeout.ms: 15000     # 15s wait for broker ACK
        delivery.timeout.ms: 60000    # 60s max total retry duration
    consumer:
      properties:
        session.timeout.ms: 45000     # 45s heartbeat timeout before rebalance
        heartbeat.interval.ms: 15000  # 15s consumer heartbeat
        max.poll.interval.ms: 300000  # 5 minutes max processing time per poll batch
```
- **Rationale**: Kafka producer blocks for at most 5 seconds during network partition. Consumers are allowed up to 5 minutes to process a batch before being marked dead and triggering partition reassignment.

### 3.5 Frontend Client Request Timeout
- Configured in React `axios` instance: `timeout: 8000` (8 seconds).
- Displays a clean error notification ("Server request timed out. Please check your network connection.") if no response arrives within 8 seconds.

