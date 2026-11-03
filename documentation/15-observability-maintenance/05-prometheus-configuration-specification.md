# Prometheus Configuration Specification

**Document Identifier:** DOC-OBS-005  
**Traceability References:** NFR-004, DOC-INF-005, DOC-OBS-003  
**Target File:** `infrastructure/prometheus/prometheus.yml`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the Prometheus monitoring server configuration, scrape targets, sampling frequencies, and alert rule linkages across the Task Management platform.

---

## 2. Complete `prometheus.yml` Specification

```yaml
global:
  scrape_interval: 15s         # Sample targets every 15 seconds
  evaluation_interval: 15s     # Evaluate alerting rules every 15 seconds
  scrape_timeout: 10s

rule_files:
  - "alert.rules.yml"          # Include Prometheus alerting rules catalogue

scrape_configs:

  # =========================================================================
  # 1. API Gateway Telemetry
  # =========================================================================
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8080']
        labels:
          service: 'api-gateway'
          tier: 'edge'

  # =========================================================================
  # 2. User Service Telemetry
  # =========================================================================
  - job_name: 'user-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['user-service:8081']
        labels:
          service: 'user-service'
          tier: 'application'

  # =========================================================================
  # 3. Task Service Telemetry
  # =========================================================================
  - job_name: 'task-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['task-service:8082']
        labels:
          service: 'task-service'
          tier: 'application'

  # =========================================================================
  # 4. Notification Service Telemetry
  # =========================================================================
  - job_name: 'notification-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['notification-service:8083']
        labels:
          service: 'notification-service'
          tier: 'application'

  # =========================================================================
  # 5. Service Discovery Telemetry
  # =========================================================================
  - job_name: 'service-discovery'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['service-discovery:8761']
        labels:
          service: 'service-discovery'
          tier: 'infrastructure'

  # =========================================================================
  # 6. Prometheus Self-Monitoring
  # =========================================================================
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

---

## 3. Data Retention and Storage Tuning
- **Retention Window**: `--storage.tsdb.retention.time=7d` (7 days historical metrics retention).
- **Persistent Mount**: Mapped to named volume `prometheus_data:/prometheus` in Docker Compose to preserve metric histories across container restarts.
- **Resource Ceiling**: Memory usage budgeted at 256 MB in `docker-compose.yml`.

