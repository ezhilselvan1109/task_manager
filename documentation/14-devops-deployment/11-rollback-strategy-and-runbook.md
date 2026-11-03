# Rollback Strategy and Runbook

**Document Identifier:** DOC-OPS-011  
**Traceability References:** NFR-002, DOC-OPS-009, DOC-INF-013  
**Target Operational Scenario:** Emergency Deployment Reversal  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This runbook specifies the emergency rollback procedures and automation scripts required to revert a failed service deployment to the last known healthy state. The strategy guarantees that rollbacks execute in **under 2 minutes** with zero data loss.

---

## 2. Automated Rollback Triggers

An emergency rollback is initiated immediately if any of the following conditions occur post-deployment:
1. Actuator health check probe fails (`/actuator/health` returns `DOWN` or times out $>10$s) for 3 consecutive intervals.
2. Gateway error logs detect HTTP 5xx responses exceeding **1.0%** of total traffic over a 2-minute sliding window.
3. Database migration scripts fail to complete or corrupt schema integrity.
4. Sev-1 defect (security breach or data loss) identified during post-deployment smoke tests.

---

## 3. Step-by-Step Rollback Execution Runbook

### Step 1: Identify Previous Healthy Image Tag
Inspect Jenkins deployment logs or Git tags to identify the previous stable image release (e.g. `v1.0.0` or `develop-prevsha`):
```powershell
docker images | findstr taskmanager
```

### Step 2: Roll Back Degraded Service Container
Execute isolated container recreation pointing to the previous stable image without disrupting unrelated microservices:
```powershell
# Example: Rolling back task-service to previous stable tag
docker stop taskmanager-task-service
docker rm taskmanager-task-service

# Launch previous stable container image
docker run -d --name taskmanager-task-service \
  --network taskmanager-network \
  --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql:5432/task_db \
  -e SPRING_DATASOURCE_USERNAME=task_app \
  -e SPRING_DATASOURCE_PASSWORD=${DB_TASK_PASSWORD} \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery:8761/eureka/ \
  taskmanager/task-service:v1.0.0
```

### Step 3: Verify Health and Eureka Registration
1. Verify container log stream:
   ```powershell
   docker logs -f taskmanager-task-service
   ```
2. Verify Actuator health:
   ```powershell
   docker exec -it taskmanager-api-gateway curl -f http://taskmanager-task-service:8082/actuator/health
   ```
3. Verify Eureka UI (`http://localhost:8761`) shows `TASK-SERVICE` as UP.

### Step 4: Database Rollback (If Schema Modified)
If the failed release applied a destructive or incompatible Flyway migration:
```powershell
# Restore target database from pre-deployment snapshot
docker exec -i taskmanager-postgresql psql -U postgres -d task_db < /backup/pre_deploy_task_db.sql
```

