# Multi-Container Docker Compose Design

**Document Identifier:** DOC-INF-005  
**Traceability References:** NFR-007, ADR-003, DOC-INF-001  
**Target File:** `infrastructure/docker-compose.yml`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides the complete, production-grade `docker-compose.yml` architectural specification for orchestrating all 11 services across the Task Management platform. The configuration enforces health checks, startup dependency sequencing, memory caps, network segregation, and persistent volumes.

---

## 2. Docker Compose Orchestration Specification

```yaml
version: '3.8'

networks:
  taskmanager-network:
    name: taskmanager-network
    driver: bridge
    ipam:
      config:
        - subnet: 172.28.0.0/16

volumes:
  postgres_data:
    name: taskmanager_postgres_data
  zookeeper_data:
    name: taskmanager_zookeeper_data
  kafka_data:
    name: taskmanager_kafka_data
  prometheus_data:
    name: taskmanager_prometheus_data
  grafana_data:
    name: taskmanager_grafana_data

services:

  # =========================================================================
  # 1. PostgreSQL Datastore (Multi-Database per Service)
  # =========================================================================
  postgresql:
    image: postgres:16-alpine
    container_name: taskmanager-postgresql
    restart: unless-stopped
    networks:
      - taskmanager-network
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_ROOT_PASSWORD:-postgres_master_secret}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts/init-multi-db.sh:/docker-entrypoint-initdb.d/init-multi-db.sh:ro
    ports:
      - "127.0.0.1:5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 512M

  # =========================================================================
  # 2. Apache Kafka & Zookeeper Messaging Tier
  # =========================================================================
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    container_name: taskmanager-zookeeper
    restart: unless-stopped
    networks:
      - taskmanager-network
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    volumes:
      - zookeeper_data:/var/lib/zookeeper/data
    deploy:
      resources:
        limits:
          memory: 256M

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    container_name: taskmanager-kafka
    restart: unless-stopped
    depends_on:
      - zookeeper
    networks:
      - taskmanager-network
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9094
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      KAFKA_JVM_PERFORMANCE_OPTS: "-Xms256m -Xmx512m"
    volumes:
      - kafka_data:/var/lib/kafka/data
    healthcheck:
      test: ["CMD", "nc", "-z", "localhost", "9092"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 768M

  # =========================================================================
  # 3. Eureka Service Discovery
  # =========================================================================
  service-discovery:
    build:
      context: ../service-discovery
      dockerfile: Dockerfile
    container_name: taskmanager-discovery
    restart: unless-stopped
    networks:
      - taskmanager-network
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SERVER_PORT: 8761
      JAVA_OPTS: "-Xms64m -Xmx192m"
    ports:
      - "127.0.0.1:8761:8761"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 256M

  # =========================================================================
  # 4. User Service
  # =========================================================================
  user-service:
    build:
      context: ../user-service
      dockerfile: Dockerfile
    container_name: taskmanager-user-service
    restart: unless-stopped
    depends_on:
      postgresql:
        condition: service_healthy
      service-discovery:
        condition: service_healthy
    networks:
      - taskmanager-network
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgresql:5432/user_db
      SPRING_DATASOURCE_USERNAME: user_app
      SPRING_DATASOURCE_PASSWORD: ${DB_USER_PASSWORD:-user_password_secret}
      JWT_SECRET: ${JWT_SECRET:-dGhpcy1pcy1hLXNlY3VyZS0yNTYtYml0LXNlY3JldC1rZXktdGFzay1tYW5hZ2Vy!}
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-discovery:8761/eureka/
      JAVA_OPTS: "-Xms128m -Xmx256m"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 384M

  # =========================================================================
  # 5. Task Service
  # =========================================================================
  task-service:
    build:
      context: ../task-service
      dockerfile: Dockerfile
    container_name: taskmanager-task-service
    restart: unless-stopped
    depends_on:
      postgresql:
        condition: service_healthy
      kafka:
        condition: service_healthy
      service-discovery:
        condition: service_healthy
    networks:
      - taskmanager-network
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgresql:5432/task_db
      SPRING_DATASOURCE_USERNAME: task_app
      SPRING_DATASOURCE_PASSWORD: ${DB_TASK_PASSWORD:-task_password_secret}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-discovery:8761/eureka/
      JAVA_OPTS: "-Xms128m -Xmx384m"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 512M

  # =========================================================================
  # 6. Notification Service
  # =========================================================================
  notification-service:
    build:
      context: ../notification-service
      dockerfile: Dockerfile
    container_name: taskmanager-notification-service
    restart: unless-stopped
    depends_on:
      postgresql:
        condition: service_healthy
      kafka:
        condition: service_healthy
      service-discovery:
        condition: service_healthy
    networks:
      - taskmanager-network
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgresql:5432/notification_db
      SPRING_DATASOURCE_USERNAME: notification_app
      SPRING_DATASOURCE_PASSWORD: ${DB_NOTIF_PASSWORD:-notification_password_secret}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-discovery:8761/eureka/
      JAVA_OPTS: "-Xms128m -Xmx256m"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 384M

  # =========================================================================
  # 7. Spring Cloud API Gateway (Edge Ingress)
  # =========================================================================
  api-gateway:
    build:
      context: ../api-gateway
      dockerfile: Dockerfile
    container_name: taskmanager-api-gateway
    restart: unless-stopped
    depends_on:
      service-discovery:
        condition: service_healthy
    networks:
      - taskmanager-network
    environment:
      SPRING_PROFILES_ACTIVE: docker
      JWT_SECRET: ${JWT_SECRET:-dGhpcy1pcy1hLXNlY3VyZS0yNTYtYml0LXNlY3JldC1rZXktdGFzay1tYW5hZ2Vy!}
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-discovery:8761/eureka/
      JAVA_OPTS: "-Xms128m -Xmx256m"
    ports:
      - "127.0.0.1:8080:8080"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          memory: 384M

  # =========================================================================
  # 8. React Web Frontend
  # =========================================================================
  frontend:
    build:
      context: ../frontend
      dockerfile: Dockerfile
    container_name: taskmanager-frontend
    restart: unless-stopped
    depends_on:
      - api-gateway
    networks:
      - taskmanager-network
    ports:
      - "127.0.0.1:3000:80"
    deploy:
      resources:
        limits:
          memory: 128M

  # =========================================================================
  # 9. Observability: Prometheus
  # =========================================================================
  prometheus:
    image: prom/prometheus:v2.51.0
    container_name: taskmanager-prometheus
    restart: unless-stopped
    networks:
      - taskmanager-network
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    ports:
      - "127.0.0.1:9090:9090"
    deploy:
      resources:
        limits:
          memory: 256M

  # =========================================================================
  # 10. Observability: Grafana
  # =========================================================================
  grafana:
    image: grafana/grafana:10.4.0
    container_name: taskmanager-grafana
    restart: unless-stopped
    depends_on:
      - prometheus
    networks:
      - taskmanager-network
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning:ro
    ports:
      - "127.0.0.1:3001:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD:-admin}
    deploy:
      resources:
        limits:
          memory: 256M
```

