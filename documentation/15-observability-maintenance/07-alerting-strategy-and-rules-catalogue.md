# Alerting Strategy and Rules Catalogue

**Document Identifier:** DOC-OBS-007  
**Traceability References:** NFR-004, NFR-005, DOC-OBS-005  
**Target File:** `infrastructure/prometheus/alert.rules.yml`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the alerting architecture, incident severity tiers, and Prometheus alerting rules across the Task Management platform. Alerts notify engineering and on-call teams of service outages, latency degradation, database pool exhaustion, and message processing anomalies before end users are impacted.

---

## 2. Alert Severity Classification

| Severity Level | Definition | Notification Channel | Response SLA |
|---|---|---|---|
| **CRITICAL** | Core service down, 5xx error rate $> 5\%$, database connection pool exhausted. | PagerDuty / Urgent Slack Broadcast | **$< 15$ minutes** |
| **WARNING** | Degradation, high p95 latency ($>500$ ms), JVM memory $>90\%$, circuit breaker tripped. | DevOps Slack Alert Channel | **$< 1$ hour** |
| **INFO** | Minor anomaly, DLT poison pill isolated, consumer lag spike self-resolving. | Informational Audit Channel | Next Business Day |

---

## 3. Prometheus Alerting Rules Specification (`alert.rules.yml`)

```yaml
groups:
  - name: taskmanager-alerts
    rules:

      # =======================================================================
      # 1. Service Liveness Alerts
      # =======================================================================
      - alert: MicroserviceDown
        expr: up{job=~"api-gateway|user-service|task-service|notification-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Microservice instance is DOWN"
          description: "Service {{ $labels.service }} has been unreachable for more than 1 minute."

      # =======================================================================
      # 2. HTTP Quality & Latency Alerts
      # =======================================================================
      - alert: HighHttp5xxErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[2m])) 
          / sum(rate(http_server_requests_seconds_count[2m])) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High HTTP 5xx Error Rate Detected"
          description: "HTTP 5xx failure rate exceeds 5% of total traffic on {{ $labels.service }}."

      - alert: HighRequestLatencyP95
        expr: |
          histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[3m])) by (le, service)) > 0.500
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "p95 HTTP Request Latency Exceeds SLA"
          description: "p95 response latency on {{ $labels.service }} is above 500ms for 3 minutes."

      # =======================================================================
      # 3. Database & Resource Saturation Alerts
      # =======================================================================
      - alert: HikariPoolConnectionExhausted
        expr: hikaricp_connections_pending > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database Connection Pool Saturated"
          description: "Threads on {{ $labels.service }} are queuing for database connections."

      - alert: JvmHeapMemoryHigh
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.90
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High JVM Heap Utilization (>90%)"
          description: "Container heap memory on {{ $labels.service }} exceeds 90% for 5 minutes."

      # =======================================================================
      # 4. Resilience & Messaging Alerts
      # =======================================================================
      - alert: CircuitBreakerTrippedOpen
        expr: resilience4j_circuitbreaker_state{state="open"} == 1
        for: 30s
        labels:
          severity: warning
        annotations:
          summary: "Gateway Circuit Breaker is OPEN"
          description: "Circuit breaker {{ $labels.name }} has tripped to OPEN, fast-failing traffic."

      - alert: DeadLetterTopicRecordReceived
        expr: increase(kafka_consumer_dlt_routed_total[5m]) > 0
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Poison Pill Routed to Kafka DLT"
          description: "One or more malformed records were diverted to task.events.v1.DLT."
```

