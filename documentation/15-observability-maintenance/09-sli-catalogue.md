# Service Level Indicators (SLI) Catalogue

**Document Identifier:** DOC-OBS-009  
**Traceability References:** NFR-001, NFR-002, DOC-OBS-001  
**Methodology:** Google SRE Service Level Indicators (SLI) Framework  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the Service Level Indicators (SLIs) for the Task Management System. An SLI is a carefully defined quantitative measurement of the service level provided by the platform.

---

## 2. SLI Master Catalogue

| SLI ID | Indicator Name | Specification & Measurement Formula | Measurement Source |
|---|---|---|---|
| `SLI-001` | **Ingress Request Availability** | $$\frac{\sum \text{Requests}_{\text{status} \ne 5xx}}{\sum \text{Total Requests}} \times 100\%$$ | `api-gateway` Actuator metrics |
| `SLI-002` | **Task Query Read Latency** | $$\frac{\sum \text{Read Requests}_{\text{latency} \le 100\text{ms}}}{\sum \text{Total Read Requests}} \times 100\%$$ | `task-service` Micrometer timer |
| `SLI-003` | **Task Mutation Latency** | $$\frac{\sum \text{Mutation Requests}_{\text{latency} \le 200\text{ms}}}{\sum \text{Total Mutation Requests}} \times 100\%$$ | `task-service` Micrometer timer |
| `SLI-004` | **User Authentication Latency**| $$\frac{\sum \text{Login Requests}_{\text{latency} \le 350\text{ms}}}{\sum \text{Total Login Requests}} \times 100\%$$ | `user-service` Micrometer timer |
| `SLI-005` | **Outbox Relay Latency** | $$\frac{\sum \text{Outbox Relays}_{\text{delay} \le 1000\text{ms}}}{\sum \text{Total Outbox Events}} \times 100\%$$ | `task-service` Outbox poller |
| `SLI-006` | **Database Acquire Latency** | $$\frac{\sum \text{Connections Acquired}_{\text{wait} \le 50\text{ms}}}{\sum \text{Total Connections Acquired}} \times 100\%$$ | HikariCP telemetry |

---

## 3. PromQL Implementation Expressions

- **`SLI-001` (Availability % over 30d rolling window)**:
  ```promql
  (
    sum(increase(http_server_requests_seconds_count{status!~"5.."}[30d]))
    / sum(increase(http_server_requests_seconds_count[30d]))
  ) * 100
  ```

- **`SLI-002` (Read Latency $< 100$ ms %)**:
  ```promql
  (
    sum(increase(http_server_requests_seconds_bucket{method="GET", le="0.1"}[30d]))
    / sum(increase(http_server_requests_seconds_count{method="GET"}[30d]))
  ) * 100
  ```

