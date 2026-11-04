# Hostinger VPS Deployment Guide (Ubuntu 26.04 LTS)

| Field | Value |
|---|---|
| **Document ID** | DOC-DEP-005 |
| **Title** | End-to-End Production Deployment Guide for Hostinger VPS |
| **Project** | Task Management System – Microservices Architecture |
| **Target OS** | Ubuntu 26.04 LTS (KVM 4: 4 vCPU, 16 GB RAM, 200 GB NVMe) |
| **Status** | Approved Production Baseline |
| **Traceability Reference** | ADR-001, ADR-002, ADR-003, ADR-004, ADR-006, ADR-008, DOC-DEP-001, DOC-DEP-003 |

---

## 1. Initial VPS Preparation & Security Hardening

Connect to your Hostinger VPS via SSH as `root` or a sudo user:

```bash
ssh root@YOUR_VPS_IP
```

### 1.1 Update Package Indexes & Upgrade OS
```bash
apt update && apt upgrade -y
apt install -y curl wget git ufw apt-transport-https ca-certificates gnupg lsb-release htop
```

### 1.2 Configure UFW Host Firewall
```bash
# Set baseline rules
ufw default deny incoming
ufw default allow outgoing

# Allow SSH, HTTP, and HTTPS
ufw allow 22/tcp comment 'SSH'
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'

# Enable firewall
ufw --force enable
ufw status verbose
```

---

## 2. Docker Engine & Docker Compose Plugin Installation

Install the official Docker Engine and modern Docker Compose v2 plugin:

```bash
# 1. Add Docker official GPG key
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# 2. Set up the Docker APT repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

# 3. Install Docker packages
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. Verify installation
docker --version
docker compose version

# 5. Enable and start Docker service
systemctl enable docker
systemctl start docker
```

---

## 3. Clone Repository & Configure Environment

### 3.1 Clone Project Repository
```bash
mkdir -p /opt/taskmanager
cd /opt/taskmanager
git clone https://github.com/ezhilselvan1109/task_manager.git .
```

### 3.2 Create Production Environment File (`.env`)
Generate cryptographically strong credentials directly on the VPS:

```bash
cat <<EOF > .env
SPRING_PROFILES_ACTIVE=docker
POSTGRES_HOST_PORT=5432
POSTGRES_ROOT_PASSWORD=$(openssl rand -base64 24)
DB_USER_PASSWORD=$(openssl rand -base64 24)
DB_TASK_PASSWORD=$(openssl rand -base64 24)
DB_NOTIF_PASSWORD=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
GRAFANA_ADMIN_PASSWORD=$(openssl rand -base64 18)
DOMAIN_NAME=YOUR_DOMAIN_OR_VPS_IP
FRONTEND_API_BASE_URL=
EOF

# Restrict file permissions
chmod 600 .env
```

Review and verify the generated `.env`:
```bash
cat .env
```

---

## 4. Validate Static Configuration

Validate the Compose configuration against the generated `.env`:

```bash
docker compose -f docker-compose.prod.yml config --quiet
```
*If this command exits with code 0 and no output, the configuration is completely valid.*

---

## 5. Build Container Images on VPS

Build all images locally on the VPS using Docker BuildKit:

```bash
# Enable BuildKit for optimal parallel builds & layer caching
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Build all images defined in docker-compose.prod.yml
docker compose -f docker-compose.prod.yml build
```

---

## 6. Service Startup in Ordered Stages

To ensure smooth startup and prevent race conditions on initial deployment, start services in sequenced tiers:

### Stage 1: Core Datastore & Message Broker
```bash
docker compose -f docker-compose.prod.yml up -d postgresql kafka
```
Wait for health checks to turn healthy:
```bash
docker compose -f docker-compose.prod.yml ps
```

### Stage 2: Kafka Topic Initialization & Service Discovery
```bash
docker compose -f docker-compose.prod.yml up -d kafka-init service-discovery
```
Wait 20 seconds, then check discovery health:
```bash
curl -f http://127.0.0.1:8761/actuator/health
```

### Stage 3: Core Microservices & API Gateway
```bash
docker compose -f docker-compose.prod.yml up -d user-service task-service notification-service api-gateway
```
Monitor microservice startup logs:
```bash
docker compose -f docker-compose.prod.yml logs -f user-service task-service notification-service
```

### Stage 4: Frontend, Observability, and Nginx Edge Proxy
```bash
docker compose -f docker-compose.prod.yml up -d frontend prometheus grafana nginx
```

---

## 7. Verification & Post-Deployment Health Checks

### 7.1 Verify Container Health Status
```bash
docker compose -f docker-compose.prod.yml ps
```
All containers should display status `Up (healthy)` or `Up`.

### 7.2 Verify PostgreSQL Databases & Dedicated Users
```bash
docker compose -f docker-compose.prod.yml exec postgresql psql -U postgres -c "\l"
docker compose -f docker-compose.prod.yml exec postgresql psql -U postgres -c "\du"
```
*Expected: `user_db` (owner: `user_app`), `task_db` (owner: `task_app`), `notification_db` (owner: `notification_app`).*

### 7.3 Verify Flyway Migrations
```bash
# Check user_db Flyway history
docker compose -f docker-compose.prod.yml exec postgresql psql -U user_app -d user_db -c "SELECT version, description, success FROM flyway_schema_history;"

# Check task_db Flyway history
docker compose -f docker-compose.prod.yml exec postgresql psql -U task_app -d task_db -c "SELECT version, description, success FROM flyway_schema_history;"

# Check notification_db Flyway history
docker compose -f docker-compose.prod.yml exec postgresql psql -U notification_app -d notification_db -c "SELECT version, description, success FROM flyway_schema_history;"
```

### 7.4 Verify Eureka Service Discovery Registration
```bash
curl -s http://127.0.0.1:8761/eureka/apps | grep "<name>"
```
*Expected instances: `API-GATEWAY`, `USER-SERVICE`, `TASK-SERVICE`, `NOTIFICATION-SERVICE`.*

### 7.5 Verify Kafka Topics
```bash
docker compose -f docker-compose.prod.yml exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```
*Expected topics: `task-events`, `task-events.DLT`.*

### 7.6 Verify Edge Ingress & API Gateway via Nginx
```bash
# Test Nginx health
curl -i http://localhost/healthz

# Test API Gateway through Nginx
curl -i http://localhost/api/v1/auth/register -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@taskmanager.com","password":"Password123!","fullName":"Admin User"}'
```

---

## 8. SSL/TLS Certificate Setup with Let's Encrypt (Certbot)

Once your DNS `A` record points your domain to the Hostinger VPS IP:

```bash
# 1. Install Certbot
apt install -y certbot

# 2. Request certificate via Nginx webroot challenge
certbot certonly --webroot \
  -w /var/lib/docker/volumes/taskmanager_certbot_www/_data \
  -d yourdomain.com -d www.yourdomain.com \
  --email your-email@example.com \
  --agree-tos --no-eff-email

# 3. Copy/link certificates into taskmanager_certbot_conf volume
docker compose -f docker-compose.prod.yml restart nginx
```

---

## 9. Safe Service Maintenance & Update Runbook

### 9.1 Updating the Application from GitHub
```bash
cd /opt/taskmanager
git pull origin main

# Rebuild only modified service images
docker compose -f docker-compose.prod.yml build

# Perform rolling restart of updated services with zero data loss
docker compose -f docker-compose.prod.yml up -d --no-deps api-gateway user-service task-service notification-service frontend
```

### 9.2 Safe Full Stack Restart
```bash
# Stop all services gracefully
docker compose -f docker-compose.prod.yml stop

# Start stack back up
docker compose -f docker-compose.prod.yml start
```

---

## 10. Automated PostgreSQL Backup & Disaster Recovery

### 10.1 Backup Script (`/opt/taskmanager/scripts/backup-db.sh`)
```bash
mkdir -p /opt/taskmanager/scripts /opt/taskmanager/backups

cat <<'EOF' > /opt/taskmanager/scripts/backup-db.sh
#!/bin/bash
set -e
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/opt/taskmanager/backups"
mkdir -p "$BACKUP_DIR"

echo "Starting automated PostgreSQL backup at $TIMESTAMP..."
docker compose -f /opt/taskmanager/docker-compose.prod.yml exec -T postgresql pg_dumpall -U postgres | gzip > "$BACKUP_DIR/pg_all_dbs_$TIMESTAMP.sql.gz"

# Retain backups for 14 days
find "$BACKUP_DIR" -type f -name "pg_all_dbs_*.sql.gz" -mtime +14 -delete
echo "Backup completed successfully: $BACKUP_DIR/pg_all_dbs_$TIMESTAMP.sql.gz"
EOF

chmod +x /opt/taskmanager/scripts/backup-db.sh
```

### 10.2 Add Daily Cron Job
```bash
# Run backup daily at 02:00 AM
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/taskmanager/scripts/backup-db.sh >> /var/log/pg_backup.log 2>&1") | crontab -
```

### 10.3 Restoring from Backup
```bash
# Decompress and restore full database cluster
gunzip < /opt/taskmanager/backups/pg_all_dbs_YYYYMMDD_HHMMSS.sql.gz | \
  docker compose -f /opt/taskmanager/docker-compose.prod.yml exec -T postgresql psql -U postgres
```

---

## 11. Monitoring Memory, CPU, and Storage on Hostinger VPS

```bash
# Real-time container resource utilization (CPU %, MEM %, NET I/O, BLOCK I/O)
docker stats --no-stream

# VPS host RAM and swap overview
free -h

# Hostinger NVMe disk usage
df -h /

# Docker storage footprint (images, containers, volumes, build cache)
docker system df
```

