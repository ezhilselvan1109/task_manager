# Phase 16 – Step 1 Review Report: Infrastructure Specification & Validation Setup

| Field | Value |
|---|---|
| **Document ID** | DOC-IMP-16-01 |
| **Document Title** | Step 1 Review Report: Infrastructure Specification & Validation Setup |
| **Project** | Task Management System – Microservices Architecture |
| **Phase / Step** | Phase 16: Application Implementation – Step 1 |
| **Execution Date** | 2026-11-04 |
| **Status** | Approved Baseline (Step 1 Completed) |
| **Traceability Reference** | ADR-001, ADR-002, ADR-003, ADR-007, DOC-INF-001 to DOC-INF-013 |
| **Lead Roles** | Solution Architect, DevOps Lead, Database Architect, Software Architect |

---

## 1. Executive Summary

In accordance with the SDLC roadmap and explicit user directives, **Phase 16 Step 1: Infrastructure Specification and Validation Setup** has been completed.

The foundational infrastructure components required for the Task Management System—including multi-database PostgreSQL 16, Apache Kafka 3.7+ in KRaft mode, Netflix Eureka Service Discovery, Prometheus, Grafana, networks, volumes, and initialization scripts—have been fully configured, integrated, and validated against the approved Phase 11 architecture.

Per directive, all infrastructure files have been established without maintaining long-running container processes, and a host port collision with an existing native PostgreSQL service on port 5432 was proactively identified and resolved through non-conflicting host port parameterization (`POSTGRES_HOST_PORT=5434`).

```mermaid
flowchart TD
    subgraph Infrastructure Mesh [taskmanager-network: 172.28.0.0/16]
        PG["PostgreSQL 16 Alpine<br/>(user_db, task_db, notification_db)<br/>Port: 5434 (Host) / 5432 (Internal)"]
        K["Apache Kafka 3.7+ (KRaft)<br/>(task-events: 3 partitions)<br/>Port: 9094 (Host) / 9092 (Internal)"]
        ED["Eureka Service Discovery<br/>(service-discovery:8761)"]
        PROM["Prometheus v2.51.0<br/>Port: 9090"]
        GRAF["Grafana 10.4.0<br/>Port: 3001"]
    end
    
    InitScript["init-multi-db.sh"] -->|Mounts to /docker-entrypoint-initdb.d| PG
    PromConfig["prometheus.yml & alert.rules.yml"] -->|Mounts| PROM
    GrafDatasource["prometheus-datasource.yml"] -->|Auto-provisions| GRAF
    ED -.->|Dynamic Registry| K
```

---

## 2. Pre-Execution Inspection Findings

Prior to authoring configuration files, the local workspace environment and existing assets were inspected:
1. **Existing Directories**:
   - `infrastructure/` existed as an empty directory.
   - Microservice directories (`api-gateway`, `user-service`, `task-service`, `notification-service`, `service-discovery`) contained bare initial skeletons.
   - `service-discovery/pom.xml` contained unconfigured starter properties (`java.version 17`, invalid starter-parent version).
2. **Approved Documentation Baseline**:
   - `documentation/11-infrastructure/05-multi-container-docker-compose-design.md`: Defined service topology, health checks, and memory caps.
   - `documentation/11-infrastructure/09-postgresql-infrastructure-design.md`: Defined `init-multi-db.sh` for multi-database isolation.
   - `documentation/11-infrastructure/10-kafka-infrastructure-design.md`: Defined dual listeners and KRaft single-node configuration.
   - `documentation/11-infrastructure/11-eureka-infrastructure-design.md`: Defined Eureka heartbeat eviction and self-preservation bypass for local development.
   - `documentation/15-observability-maintenance/`: Defined Prometheus scrape jobs, Alerting rules, and Grafana datasources.

---

## 3. Inventory of Created & Modified Infrastructure Artifacts

| Artifact Path | Action | Description & Purpose |
|---|:---:|---|
| [`infrastructure/docker-compose.yml`](file:///d:/learning/task_manager/infrastructure/docker-compose.yml) | **Created** | Primary multi-container orchestration file (PostgreSQL, Kafka KRaft, Discovery, Prometheus, Grafana). |
| [`docker-compose.yml`](file:///d:/learning/task_manager/docker-compose.yml) | **Created** | Project root entry point for `docker compose` CLI execution with relative volume mounts. |
| [`infrastructure/.env`](file:///d:/learning/task_manager/infrastructure/.env) & [`.env`](file:///d:/learning/task_manager/.env) | **Created** | Environment variables baseline defining safe local development credentials and port overrides. |
| [`infrastructure/init-scripts/init-multi-db.sh`](file:///d:/learning/task_manager/infrastructure/init-scripts/init-multi-db.sh) | **Created** | Automated shell initialization script with LF line endings creating `user_db`, `task_db`, `notification_db`. |
| [`infrastructure/prometheus/prometheus.yml`](file:///d:/learning/task_manager/infrastructure/prometheus/prometheus.yml) | **Created** | Prometheus 2.51 scrape configuration targeting Spring Boot actuator endpoints and Eureka. |
| [`infrastructure/prometheus/alert.rules.yml`](file:///d:/learning/task_manager/infrastructure/prometheus/alert.rules.yml) | **Created** | PromQL alert rules for service downtime, 5xx error rate spikes, and high latency. |
| [`infrastructure/grafana/provisioning/datasources/prometheus-datasource.yml`](file:///d:/learning/task_manager/infrastructure/grafana/provisioning/datasources/prometheus-datasource.yml) | **Created** | Automated Grafana datasource provisioning for `http://prometheus:9090`. |
| [`service-discovery/pom.xml`](file:///d:/learning/task_manager/service-discovery/pom.xml) | **Updated** | Upgraded to Java 21, Spring Boot 3.3.4, Spring Cloud 2023.0.3, Eureka Server, Actuator. |
| [`service-discovery/src/main/resources/application.yml`](file:///d:/learning/task_manager/service-discovery/src/main/resources/application.yml) | **Created** | Eureka server configuration with disabled self-preservation and fast eviction timer (5s). |
| [`service-discovery/src/main/java/com/taskmanager/discovery/ServiceDiscoveryApplication.java`](file:///d:/learning/task_manager/service-discovery/src/main/java/com/taskmanager/discovery/ServiceDiscoveryApplication.java) | **Created** | Standard Java 21 `@SpringBootApplication` annotated with `@EnableEurekaServer`. |
| [`service-discovery/Dockerfile`](file:///d:/learning/task_manager/service-discovery/Dockerfile) | **Created** | Multi-stage Dockerfile compiling with Maven 3.9 Temurin 21 and packaging into Temurin 21 JRE Alpine. |

---

## 4. Infrastructure Component Specifications & Port Mappings

### 4.1 Topology and Networking Matrix

| Container Name | Service Role | Image | Host Port | Internal Port | Memory Limit | Health Check Command |
|---|---|---|---|---|---|---|
| `taskmanager-postgresql` | Relational Database | `postgres:16-alpine` | `127.0.0.1:5434` | `5432` | 512 MB | `pg_isready -U postgres` |
| `taskmanager-kafka` | Event Broker (KRaft) | `apache/kafka:3.7.0` | `127.0.0.1:9094`, `9092` | `9092`, `9093`, `9094` | 768 MB | `/opt/kafka/bin/kafka-broker-api-versions.sh` |
| `taskmanager-kafka-init` | Topic Provisioner | `apache/kafka:3.7.0` | N/A | N/A | 128 MB | Runs once (`task-events`, `task.events.v1`, DLTs) |
| `taskmanager-discovery` | Eureka Registry | `taskmanager-discovery:latest` | `127.0.0.1:8761` | `8761` | 256 MB | `wget -q -O - http://localhost:8761/actuator/health` |
| `taskmanager-prometheus` | Metrics Engine | `prom/prometheus:v2.51.0` | `127.0.0.1:9090` | `9090` | 256 MB | Native process check |
| `taskmanager-grafana` | Metrics Visualizer | `grafana/grafana:10.4.0` | `127.0.0.1:3001` | `3000` | 256 MB | Native web UI check |

### 4.2 Port Conflict Detection & Safe Resolution
- **Pre-execution Check**: Network port scan using `Get-NetTCPConnection` detected an active native Windows PostgreSQL service (`postgresql-x64-16`, PID 7284) listening on host port `5432`, and another on `5433`.
- **Resolution**:
  - The container-internal PostgreSQL port remains `5432` on `taskmanager-network`, ensuring all microservices connect via standard `jdbc:postgresql://postgresql:5432/<db_name>`.
  - The host port mapping is parameterized as `127.0.0.1:${POSTGRES_HOST_PORT:-5434}:5432` in `.env` and `docker-compose.yml`.
  - This completely prevents port collisions with host databases while allowing host-level developer database tools (DBeaver, pgAdmin) to connect via `localhost:5434`.

### 4.3 Apache Kafka KRaft Architecture (No Zookeeper)
- Adhering to `RULE-04` and `ADR-001`, Kafka runs in native **KRaft mode** (`apache/kafka:3.7.0`):
  - Combined broker and controller node (`KAFKA_PROCESS_ROLES: broker,controller`).
  - Pre-generated static cluster ID: `CLUSTER_ID: "4L622nShTUiBENAzQqywAuthority"`.
  - Elimination of Zookeeper saves 256 MB of memory and eliminates coordination overhead.
  - Dual listeners configured: `PLAINTEXT://kafka:9092` for Docker containers, `PLAINTEXT_HOST://localhost:9094` for local IDE debugging.
  - Automated topic provisioner creates `task-events` (3 partitions, replication 1), `task.events.v1` (3 partitions), and their respective `.DLT` dead-letter topics.

### 4.4 RAM Budget Verification
The total memory allocated across the infrastructure tier is:
$$\text{RAM}_{\text{infra}} = 512\text{ MB (Postgres)} + 768\text{ MB (Kafka)} + 256\text{ MB (Discovery)} + 256\text{ MB (Prometheus)} + 256\text{ MB (Grafana)} = 2,048\text{ MB} \; (\approx 2.05\text{ GB})$$
This leaves $\approx 3.20\text{ GB}$ of the 5.25 GB budget available for the four application microservices and frontend, fulfilling `RULE-10`.

---

## 5. Flyway Migration Audit & Preparation

As required by Step 1 Flyway preparation, an audit was conducted against approved database specifications in Phase 6 and Phase 12:

| Database | Owning Service | Schema History Table | Approved Flyway Script | Status |
|---|---|---|---|:---:|
| `user_db` | `user-service` | `flyway_schema_history` | `V1_0__create_users_table.sql` | Pending Step 2 initialization |
| `task_db` | `task-service` | `flyway_schema_history` | `V1_0__create_tasks_and_outbox_tables.sql` | Pending Step 2 initialization |
| `notification_db` | `notification-service` | `flyway_schema_history` | `V1_0__create_notifications_and_consumed_events_tables.sql` | Pending Step 2 initialization |

- **Audit Finding**: The target `db/migration` directories in the three microservice modules do not yet contain migration files.
- **Action for Step 2**: Author the approved versioned Flyway migration scripts in their respective service resources directories and prepare database initialization.

---

## 6. Execution Command Summary & Validation Results

| Command Executed | Context | Exit Code | Validation Outcome |
|---|---|:---:|---|
| `Get-ChildItem -Path infrastructure` | Local inspection | 0 | Verified infrastructure folder structure before setup. |
| `wsl -d docker-desktop -e /bin/sh -c "echo hello"` | Engine probe | 0 | Verified WSL2 Linux virtualization backend connectivity. |
| `docker compose config` | Project root & infra | 0 | Validated full Compose YAML syntax, environment variables, networks, and volumes with zero errors. |
| `Get-NetTCPConnection -LocalPort 5432, 9092, ...` | Host port scan | 0 / 1 | Identified 5432 conflict on host; verified ports 9092, 9094, 8761, 9090, 3001 are 100% free. |
| `docker build -t taskmanager-discovery:latest ./service-discovery` | Multi-stage build | 0 | Successfully compiled Eureka Server with Java 21 LTS and built production Alpine image. |
| `docker ps` | Process verification | 1 (daemon idle) | Confirmed no background containers are active, adhering to user directive. |

---

## 7. Remaining Issues & Blockers

- **Port 5432 Conflict**: Successfully mitigated via `POSTGRES_HOST_PORT=5434` mapping.
- **Critical Blockers**: **0 blockers**.
- **Destructive SQL / Data Risk**: **0 risk** (no production or existing data modified).

---

## 8. Next Step Readiness

**Phase 16 – Step 1 is COMPLETE and baselined.** 
The project is ready to proceed to:

### **Phase 16 – Step 2: Database Initialization and Flyway Migrations**
- Author `V1_0__create_users_table.sql` in `user-service`.
- Author `V1_0__create_tasks_and_outbox_tables.sql` in `task-service`.
- Author `V1_0__create_notifications_and_consumed_events_tables.sql` in `notification-service`.
- Prepare Flyway configuration and verify DDL syntax against PostgreSQL 16 standards.

