# Grafana Dashboard Design Document

**Document Identifier:** DOC-OBS-006  
**Traceability References:** NFR-004, DOC-INF-005, DOC-OBS-003, DOC-OBS-005  
**Visualization Tool:** Grafana 10.4.x  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the Grafana visualization dashboards, panel layouts, automated provisioning configs, and PromQL telemetry queries for real-time monitoring of the Task Management Microservices platform.

---

## 2. Automated Datasource Provisioning

Configured in `infrastructure/grafana/provisioning/datasources/prometheus.yml`:

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false
```

---

## 3. Core Operational Dashboards Layout

```mermaid
graph TD
    subgraph "Grafana Dashboard Suite (Port 3001)"
        D1["Dashboard 1: Microservices Mesh Overview\n- Gateway RPS Throughput\n- p95 & p99 Latency per Endpoint\n- 5xx Error Rate Percentage\n- Service Uptime Map"]
        D2["Dashboard 2: JVM & Resource Telemetry\n- Heap Memory Usage % vs Ceilings\n- Garbage Collection Pauses (ms)\n- Container CPU Utilization %"]
        D3["Dashboard 3: Database & HikariCP Pools\n- Active vs Idle Connections\n- Pending Connection Wait Queue\n- JDBC Acquire Timeout Count"]
        D4["Dashboard 4: Kafka Messaging & Outbox\n- Transactional Outbox Pending Depth\n- Consumer Lag per Partition\n- DLT Poison Pill Ingestion Rate"]
    end
```

---

## 4. PromQL Telemetry Query Catalogue

### 4.1 System Mesh Overview Queries
- **Throughput (Requests Per Second)**:
  ```promql
  sum(rate(http_server_requests_seconds_count[1m])) by (service)
  ```
- **p95 HTTP Request Latency**:
  ```promql
  histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le, service))
  ```
- **HTTP 5xx Error Rate Percentage**:
  ```promql
  sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) 
  / sum(rate(http_server_requests_seconds_count[1m])) * 100
  ```
- **Service Liveness Grid**:
  ```promql
  up{job=~"api-gateway|user-service|task-service|notification-service|service-discovery"}
  ```

### 4.2 Database & Messaging Queries
- **HikariCP Connection Pool Saturation %**:
  ```promql
  hikaricp_connections_active / (hikaricp_connections_active + hikaricp_connections_idle) * 100
  ```
- **Pending Outbox Queue Depth**:
  ```promql
  task_outbox_pending_gauge
  ```
- **Kafka Consumer Lag**:
  ```promql
  sum(kafka_consumer_records_lag{group="notification-service-group"}) by (topic, partition)
  ```
- **Circuit Breaker Open State**:
  ```promql
  resilience4j_circuitbreaker_state{state="open"}
  ```

