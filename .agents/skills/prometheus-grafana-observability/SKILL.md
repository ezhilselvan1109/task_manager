---
name: prometheus-grafana-observability
description: >-
  Use this skill when configuring Prometheus scrape targets, PromQL alert rules, Grafana dashboard
  visualizations, and Spring Boot Actuator Micrometer metrics.
---

# Prometheus & Grafana Observability Skill

## Purpose
Governs the collection of system and application metrics via Micrometer, Prometheus scraping configurations, Grafana dashboard panels, and alerting rules.

## When to Use
- Adding custom business metrics (e.g. `tasks_created_total`, `outbox_lag_seconds`).
- Configuring Prometheus targets in `infrastructure/prometheus/prometheus.yml`.
- Creating or editing Grafana dashboard JSON models.
- Defining PromQL threshold alert rules.

## Required Inputs
- Target metrics specification from [Metrics Strategy](file:///d:/learning/task_manager/documentation/15-observability-maintenance/03-metrics-strategy-and-micrometer-design.md).
- Prometheus config `prometheus.yml`.

## Step-by-Step Procedure
1. **Instrument Code with Micrometer**:
   Inject `MeterRegistry` and record metrics:
   ```java
   @Service
   @RequiredArgsConstructor
   public class TaskServiceImpl implements TaskService {
       private final MeterRegistry meterRegistry;

       public TaskResponse createTask(CreateTaskRequest request, UUID userId) {
           ...
           meterRegistry.counter("tasks_created_total", "priority", request.priority().name()).increment();
           return response;
       }
   }
   ```
2. **Configure Prometheus Scrape Jobs**:
   In `infrastructure/prometheus/prometheus.yml`:
   ```yaml
   scrape_configs:
     - job_name: 'spring-boot-services'
       metrics_path: '/actuator/prometheus'
       scrape_interval: 10s
       static_configs:
         - targets: ['user-service:8081', 'task-service:8082', 'notification-service:8083', 'api-gateway:8080']
   ```
3. **Define PromQL Alert Rules**:
   In `infrastructure/prometheus/alert.rules.yml`:
   ```yaml
   groups:
     - name: task-manager-alerts
       rules:
         - alert: ServiceDown
           expr: up{job="spring-boot-services"} == 0
           for: 1m
           labels:
             severity: critical
           annotations:
             summary: "Instance {{ $labels.instance }} down"
   ```
4. **Provision Grafana Dashboards**:
   Store JSON models in `infrastructure/grafana/dashboards/` for automated dashboard provisioning on container startup.

## Applicable Project Standards
- [Rule 10: Observability & Operational Standards](file:///d:/learning/task_manager/.agents/rules/10-observability-and-ops.md)
- [Grafana Dashboard Design](file:///d:/learning/task_manager/documentation/15-observability-maintenance/06-grafana-dashboard-design-document.md)

## Validation Checklist
- [ ] `/actuator/prometheus` is enabled and reachable.
- [ ] Custom metrics use dimensional tags (`priority`, `status`), not dynamic label explosions.
- [ ] Prometheus scrape job targets resolve in Docker network.
- [ ] Alert rules specify proper thresholds and hold durations (`for: 1m`).

## Common Mistakes
- Using high-cardinality values (e.g. user IDs or timestamps) as metric tag values.
- Forgetting to register Actuator endpoints in Spring Security filter chain.

## Completion Criteria
Metrics scrape continuously without errors, dashboard graphs render real-time statistics, and alert rules fire as designed.

