# System Context Diagram (C4 Level 1): Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-ARCH-003 |
| **C4 Level** | Level 1: System Context |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Solution Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-ARCH-001`, `DOC-REQ-003` |

---

## 1. Overview
The C4 System Context diagram provides a high-level view of the **Task Management System (TMS)**, depicting its external human actors, boundary definitions, and interactions with external platforms.

---

## 2. C4 Level 1 System Context Diagram

```mermaid
flowchart TD
    User["Individual User\n[Person]\nKnowledge worker who manages personal tasks, priorities, and deadlines."]
    DevOps["DevOps & Site Reliability Engineer\n[Person]\nMonitors system uptime, resource utilization, and CI/CD pipelines."]

    subgraph TMSBoundary ["Task Management System (TMS) [Software System]"]
        CoreSystem["Task Management System\nProvides secure user authentication, responsive task management, productivity metrics, and asynchronous in-app notifications."]
    end

    MonitoringSystem["Prometheus & Grafana\n[External Tooling]\nAggregates metrics, evaluates alerting thresholds, and renders operational dashboards."]
    CISystem["Jenkins CI/CD\n[External Pipeline]\nAutomates Maven builds, executes test suites, and generates container images."]

    User -->|Uses web browser to manage tasks\n[HTTPS / REST]| CoreSystem
    DevOps -->|Inspects telemetry & logs\n[HTTP]| MonitoringSystem
    DevOps -->|Triggers builds & reviews tests\n[HTTPS]| CISystem

    CoreSystem -.->|Exports JVM & application metrics\n[HTTP / Micrometer]| MonitoringSystem
    CISystem -.->|Builds, tests, and packages\n[Docker / Maven]| CoreSystem
```

---

## 3. Element Catalogue

| Element Name | C4 Type | Description | Primary Protocol |
|---|---|---|---|
| **Individual User** | Person | End-user managing personal daily to-dos, priorities, and deadlines. | HTTPS / Browser |
| **DevOps Engineer** | Person | Operational engineer monitoring uptime, metrics, and CI pipelines. | HTTPS / Web UI |
| **Task Management System** | Software System | The complete distributed solution encompassing Gateway, 3 microservices, Eureka, Kafka, and DBs. | HTTPS / REST / TCP |
| **Prometheus & Grafana** | External System | Open-source observability stack scraping metrics from `/actuator/prometheus`. | HTTP Pull |
| **Jenkins CI/CD** | External System | Automation engine compiling, testing, and containerizing software artifacts. | Git Webhook / CLI |

---

## 4. Review Checklist
- [x] Standard C4 Level 1 System Context conventions adhered to.
- [x] Human actors, software system boundary, and external systems clearly delineated.
- [x] Interaction protocols and data flows labeled.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Solution Architect | Initial baseline System Context Diagram. |

