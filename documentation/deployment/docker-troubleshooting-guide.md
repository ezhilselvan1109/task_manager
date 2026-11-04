# Docker Production Troubleshooting & Diagnostic Guide

| Field | Value |
|---|---|
| **Document ID** | DOC-DEP-009 |
| **Title** | Production Troubleshooting Runbook & Failure Remediation Guide |
| **Project** | Task Management System – Microservices Architecture |
| **Target Platform** | Hostinger VPS (Ubuntu 26.04 LTS, Docker Engine 26.x) |
| **Status** | Approved Production Baseline |
| **Traceability Reference** | ADR-001, ADR-002, ADR-003, ADR-006, DOC-DEP-003, DOC-DEP-005 |

---

## 1. Primary Diagnostic Commands

Execute these commands on your Hostinger VPS to assess system state:

```bash
# 1. Check all container statuses and health states
docker compose -f docker-compose.prod.yml ps

# 2. View unified real-time logs across the entire mesh
docker compose -f docker-compose.prod.yml logs -f --tail=100

# 3. View logs for an individual failing service
docker compose -f docker-compose.prod.yml logs -f <service-name>

# 4. Check container resource consumption (CPU, RAM, Network I/O)
docker stats --no-stream

# 5. Inspect why a container exited or was terminated
docker inspect <container-name> --format='{{json .State}}' | jq .

# 6. Check host system free memory and disk space
free -h
df -h /
```

---

## 2. Common Failure Scenarios & Step-by-Step Remediation

### Scenario 1: Nginx Returns `502 Bad Gateway`

**Symptoms**: Browser requests to `https://yourdomain.com/api/...` or `/` return HTTP 502.  
**Probable Causes**:
1. `api-gateway` or `frontend` container is still starting up or in an unhealthy state.
2. Internal DNS resolution failed between Nginx and upstream containers.

**Diagnostic Steps**:
```bash
# Check upstream container health
docker compose -f docker-compose.prod.yml ps frontend api-gateway

# Check Nginx error logs
docker compose -f docker-compose.prod.yml logs --tail=50 nginx

# Test gateway connectivity directly from Nginx container
docker compose -f docker-compose.prod.yml exec nginx wget -q -O - http://api-gateway:8080/actuator/health
```

**Remediation**:
Restart upstream service or ensure healthcheck has turned green before sending requests.

---

### Scenario 2: PostgreSQL Database Authentication or Role Failure

**Symptoms**: Spring Boot microservices fail on startup with `org.postgresql.util.PSQLException: password authentication failed for user "user_app"`.  
**Probable Causes**:
1. PostgreSQL volume was initialized previously with default passwords before `.env` was created.
2. `init-multi-db.sh` was not executed because `/var/lib/postgresql/data` was not empty.

**Diagnostic Steps**:
```bash
# Test manual psql connection as master postgres user
docker compose -f docker-compose.prod.yml exec postgresql psql -U postgres -c "\du"
```

**Remediation**:
If initial credentials were changed in `.env` after the volume was already created:
```bash
# Reset password for dedicated application role
docker compose -f docker-compose.prod.yml exec postgresql psql -U postgres -c "ALTER USER user_app WITH PASSWORD 'NEW_PASSWORD_FROM_ENV';"
docker compose -f docker-compose.prod.yml exec postgresql psql -U postgres -c "ALTER USER task_app WITH PASSWORD 'NEW_PASSWORD_FROM_ENV';"
docker compose -f docker-compose.prod.yml exec postgresql psql -U postgres -c "ALTER USER notification_app WITH PASSWORD 'NEW_PASSWORD_FROM_ENV';"

# Restart microservices
docker compose -f docker-compose.prod.yml restart user-service task-service notification-service
```

---

### Scenario 3: Kafka Cluster ID Mismatch or Startup Failure

**Symptoms**: Kafka container exits with error `The node is not formatted with cluster id ...`.  
**Probable Causes**:
Kafka data directory (`kafka_data`) retains metadata from a different cluster formatting run.

**Remediation**:
```bash
# Stop kafka services
docker compose -f docker-compose.prod.yml stop kafka kafka-init

# Reformat storage volume if cluster state is corrupted
docker volume rm taskmanager_kafka_data

# Recreate and initialize
docker compose -f docker-compose.prod.yml up -d kafka
docker compose -f docker-compose.prod.yml up -d kafka-init
```

---

### Scenario 4: Microservice Fails to Register with Eureka

**Symptoms**: `api-gateway` returns 503 or routes fail with `No instances available for user-service`.  
**Probable Causes**:
1. `service-discovery` container started after microservices or was not fully healthy.
2. Eureka heartbeat eviction timer has not refreshed.

**Diagnostic Steps**:
```bash
# Inspect registered applications in Eureka registry
curl -s http://127.0.0.1:8761/eureka/apps
```

**Remediation**:
```bash
# Restart Eureka discovery first, then microservices
docker compose -f docker-compose.prod.yml restart service-discovery
sleep 15
docker compose -f docker-compose.prod.yml restart api-gateway user-service task-service notification-service
```

---

### Scenario 5: Flyway Schema Migration Lock

**Symptoms**: Microservice hangs during startup with log message `Waiting for changelog lock....` or Flyway migration fails.  
**Probable Causes**:
Container crashed or was stopped while Flyway migration was in flight.

**Remediation**:
```bash
# Inspect and unlock Flyway table
docker compose -f docker-compose.prod.yml exec postgresql psql -U user_app -d user_db -c "SELECT * FROM flyway_schema_history;"

# If a row has success = false, investigate migration failure and repair
# Or manually remove failed entry if migration was rolled back
docker compose -f docker-compose.prod.yml exec postgresql psql -U user_app -d user_db -c "DELETE FROM flyway_schema_history WHERE success = false;"

# Restart service
docker compose -f docker-compose.prod.yml restart user-service
```

---

### Scenario 6: Disk Space Exhaustion on Hostinger VPS

**Symptoms**: Docker commands fail with `no space left on device`.  
**Probable Causes**:
Unused Docker build cache layers, old dangling images, or accumulated log files.

**Diagnostic Steps**:
```bash
df -h /
docker system df
```

**Remediation**:
```bash
# Clean up dangling images, unused build cache, and stopped containers
docker system prune -af --volumes=false

# Clean Docker build cache specifically
docker builder prune -af

# Rotate and truncate oversized container logs if needed
truncate -s 0 /var/lib/docker/containers/*/*-json.log
```

---

### Scenario 7: `taskmanager-nginx` Stuck in `Created` Status or Crashing on Boot

**Symptoms**: In Hostinger Docker Manager or `docker compose ps`, `taskmanager-nginx` displays status `Created` or restarts repeatedly.  
**Probable Causes**:
1. Missing SSL certificates referenced in `app.conf` before Certbot has run.
2. Dependent containers (`frontend` or `api-gateway`) failed their initial health check during compose startup.

**Diagnostic Steps**:
```bash
docker compose -f docker-compose.prod.yml logs nginx
```

**Remediation**:
1. Ensure `infrastructure/nginx/conf.d/app.conf` runs in HTTP-first mode on port 80 (the HTTPS 443 block must be commented out until domain certificates are generated).
2. Start the Nginx container:
```bash
docker compose -f docker-compose.prod.yml up -d nginx
```
3. Verify status:
```bash
docker compose -f docker-compose.prod.yml ps nginx
```

---

### Scenario 8: Browser Shows `ERR_CONNECTION_TIMED_OUT` on Port 9090, 3001, or 8761

**Symptoms**: Clicking the port links in Hostinger Docker Manager or browsing `http://<VPS_IP>:9090` or `http://<VPS_IP>:3001` results in `ERR_CONNECTION_TIMED_OUT`.  
**Probable Causes**:
1. The container ports were bound to loopback `127.0.0.1` rather than `0.0.0.0`.
2. Ubuntu UFW host firewall or Hostinger Cloud Firewall is dropping incoming connections on ports 3001, 9090, 8761.

**Diagnostic Steps**:
```bash
# Check listening ports on VPS host
ss -tulpn | grep -E ':(9090|3001|8761)'

# Check UFW firewall status
sudo ufw status verbose
```

**Remediation**:
1. Ensure `docker-compose.prod.yml` maps ports to `"${PORT:-XXXX}:XXXX"`.
2. Allow ports in UFW:
```bash
sudo ufw allow 3001/tcp comment 'Grafana'
sudo ufw allow 9090/tcp comment 'Prometheus'
sudo ufw allow 8761/tcp comment 'Eureka'
sudo ufw reload
```
3. If using Hostinger Cloud Firewall in hPanel (Security -> Firewall), ensure a rule allows inbound TCP traffic on ports 80, 443, 3001, 9090, and 8761.
4. Alternatively, Grafana can be accessed directly on port 80 via reverse proxy: `http://<VPS_IP>/grafana/`.


