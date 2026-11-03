# Container Diagram (C4 Level 2): Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-004 |
| **C4 Level** | Level 2: Container Diagram |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-ARCH-001`, `DOC-ARCH-003` |

---

## 1. Overview
The C4 Container Diagram zooms inside the Task Management System boundary, showing the high-level shape of the software architecture, container responsibilities, communication technologies, and data stores.

---

## 2. C4 Level 2 Container Diagram

```mermaid
flowchart TB
    User["Individual User\n[Person]"]

    subgraph TMS ["Task Management System Boundary"]
        SPA["Frontend SPA\n[Container: React 18 / TypeScript]\nDelivers interactive task board, dashboard, and notification center in browser."]
        
        GW["API Gateway\n[Container: Spring Boot 3.3 / Netty]\nPort 8080. Ingress reverse proxy, JWT auth verification, rate limiting, routing."]
        
        Eureka["Service Discovery\n[Container: Eureka Server]\nPort 8761. In-memory service registry for dynamic instance resolution."]

        US["User Service\n[Container: Spring Boot 3.3 / Tomcat]\nPort 8081. Manages users, BCrypt password hashing, JWT signing, refresh tokens."]
        
        TS["Task Service\n[Container: Spring Boot 3.3 / Tomcat]\nPort 8082. Manages task CRUD, state transitions, metrics, and outbox event publishing."]
        
        NS["Notification Service\n[Container: Spring Boot 3.3 / Tomcat]\nPort 8083. Consumes task events from Kafka, persists in-app notifications, mark-as-read."]

        UDB[("User Database\n[Container: PostgreSQL 16]\nPort 5432. Stores users, profiles, and refresh tokens.")]
        
        TDB[("Task Database\n[Container: PostgreSQL 16]\nPort 5432. Stores tasks and transactional outbox events.")]
        
        NDB[("Notification Database\n[Container: PostgreSQL 16]\nPort 5432. Stores notifications and consumed event IDs.")]

        Kafka["Event Streaming Broker\n[Container: Apache Kafka 3.7+]\nPort 9092. Distributed log topic: task.events.v1."]
    end

    User -->|Visits app & manages tasks\n[HTTPS / JSON]| SPA
    SPA -->|API requests via JWT\n[REST / JSON]| GW

    GW <-->|Heartbeat & Dynamic Routing\n[HTTP]| Eureka
    US <-->|Registers & Heartbeats\n[HTTP]| Eureka
    TS <-->|Registers & Heartbeats\n[HTTP]| Eureka
    NS <-->|Registers & Heartbeats\n[HTTP]| Eureka

    GW -->|Routes /auth & /users\n[HTTP / REST]| US
    GW -->|Routes /tasks\n[HTTP / REST]| TS
    GW -->|Routes /notifications\n[HTTP / REST]| NS

    US -->|Reads/Writes user data\n[JDBC / SQL]| UDB
    TS -->|Reads/Writes tasks & outbox\n[JDBC / SQL]| TDB
    NS -->|Reads/Writes notifications\n[JDBC / SQL]| NDB

    TS -.->|Publishes task events\n[Kafka Protocol / TCP]| Kafka
    Kafka -.->|Consumes task events\n[Kafka Protocol / TCP]| NS
```

---

## 3. Container Specifications Table

| Container Name | Type | Tech Baseline | Host / Port | Responsibility |
|---|---|---|---|---|
| **Frontend SPA** | Web SPA | React 18, TypeScript, TailwindCSS, Vite | Host: 3000 | Client presentation layer; renders task boards, dashboard widgets, and notification drawer. |
| **API Gateway** | Microservice | Spring Boot 3.3, Spring Cloud Gateway | Host: 8080 | Edge ingress proxy, JWT validation, header injection (`X-User-Id`), CORS, routing. |
| **Service Discovery** | Microservice | Spring Cloud Netflix Eureka | Host: 8761 | Service registry maintaining real-time heartbeat and location of active microservices. |
| **User Service** | Microservice | Spring Boot 3.3, Spring Data JPA, Java 21 | Host: 8081 | Identity lifecycle, credential verification, JWT issuance, profile updates. |
| **Task Service** | Microservice | Spring Boot 3.3, Spring Data JPA, Java 21 | Host: 8082 | Personal task management, filtering, metrics aggregation, Transactional Outbox publishing. |
| **Notification Service** | Microservice | Spring Boot 3.3, Spring Data JPA, Java 21 | Host: 8083 | In-app notification management, idempotent Kafka consumer. |
| **User Database** | Relational DB | PostgreSQL 16 | Internal :5432 | Dedicated database `user_db` storing user records and refresh tokens. |
| **Task Database** | Relational DB | PostgreSQL 16 | Internal :5432 | Dedicated database `task_db` storing task records and `outbox_events`. |
| **Notification Database**| Relational DB | PostgreSQL 16 | Internal :5432 | Dedicated database `notification_db` storing notifications and `consumed_events`. |
| **Kafka Broker** | Message Broker | Apache Kafka 3.7+ | Host: 9092 | Asynchronous message broker hosting `task.events.v1` and dead letter topics. |

---

## 4. Review Checklist
- [x] Standard C4 Level 2 Container Diagram conventions applied.
- [x] All 10 deployable containers, port allocations, and technologies documented.
- [x] Database-per-service isolation and asynchronous Kafka decoupling verified.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline Container Diagram. |

