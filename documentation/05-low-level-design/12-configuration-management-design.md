# Configuration Management Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-012 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | DevOps Lead & Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-ARCH-006` |

---

## 1. Overview & 12-Factor App Compliance
Configuration follows **The Twelve-Factor App (Factor III: Config)** by strictly separating configuration from code. All environment-dependent parameters (database credentials, Kafka brokers, Eureka URLs, JWT secrets) are externalized into environment variables with sensible local defaults in `application.yml`.

---

## 2. Standard Service Configuration (`task-service/src/main/resources/application.yml`)

```yaml
server:
  port: ${PORT:8082}
  shutdown: graceful

spring:
  application:
    name: task-service
  threads:
    virtual:
      enabled: true

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:task_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      pool-name: TaskServiceHikariPool

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        show_sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_HOST:localhost}:${EUREKA_PORT:8761}/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

app:
  outbox:
    poll-interval: 1000
    batch-size: 50
```

---

## 3. Configuration Profiles Matrix

| Profile Name | Target Environment | Database Configuration | Kafka Broker | Flyway Action | Logging Level |
|---|---|---|---|---|---|
| `default` / `local` | Local IDE Execution | `localhost:5432` | `localhost:9092` | Migrate automatically | `DEBUG` on `com.chellaa` |
| `docker` | Docker Compose Topology | `tms-postgres:5432` | `tms-kafka:9092` | Migrate automatically | `INFO` |
| `test` | Automated Unit/Integ Tests | Testcontainers / H2 | Embedded Kafka | Test schema setup | `WARN` |
| `prod` | Production / Staging | Managed PostgreSQL Cluster | Clustered Brokers | Pre-deployment migration | `INFO` / `WARN` |

---

## 4. Review Checklist
- [x] Standard `application.yml` structure established with sensible local defaults.
- [x] HikariCP connection pool settings tuned for resource stability.
- [x] 12-factor externalized environment variable names standardized.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | DevOps Lead | Initial baseline Configuration Management Design. |

