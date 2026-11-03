# Environment Variable Catalogue

**Document Identifier:** DOC-INF-007  
**Traceability References:** SEC-005, NFR-007, DOC-INF-005  
**Target Environments:** All Containers & Workstations  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document provides the definitive master catalogue of all environment variables across the Task Management platform. It defines their purpose, target services, default fallback values, and security classifications.

---

## 2. Master Environment Variable Catalogue

| Variable Name | Target Container(s) | Required? | Default / Example Value | Sensitivity | Purpose & Description |
|---|---|---|---|---|---|
| `POSTGRES_ROOT_PASSWORD` | `postgresql` | Yes | `postgres_master_secret` | Critical | Superuser password for initial PostgreSQL provisioning. |
| `DB_USER_PASSWORD` | `user-service`, `postgresql` | Yes | `user_password_secret` | High | Password for role `user_app` on `user_db`. |
| `DB_TASK_PASSWORD` | `task-service`, `postgresql` | Yes | `task_password_secret` | High | Password for role `task_app` on `task_db`. |
| `DB_NOTIF_PASSWORD` | `notification-service`, `postgresql` | Yes | `notification_password_secret`| High | Password for role `notification_app` on `notification_db`. |
| `JWT_SECRET` | `user-service`, `api-gateway` | Yes | `dGhpcy1pcy1hLXNlY3VyZS0y...` | Critical | 256-bit symmetric key for HMAC-SHA256 JWT signing/validation. |
| `SPRING_PROFILES_ACTIVE` | All Microservices | Yes | `docker` | Low | Activates environment configuration (`docker`, `local`, `test`). |
| `SPRING_DATASOURCE_URL` | Microservices | Yes | `jdbc:postgresql://postgresql:5432/...`| Low | JDBC connection string to the dedicated database. |
| `SPRING_DATASOURCE_USERNAME`| Microservices | Yes | `task_app` / `user_app` | Medium | Database login role name. |
| `SPRING_DATASOURCE_PASSWORD`| Microservices | Yes | Injected from `DB_*_PASSWORD` | High | Database login credential. |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS`| `task-service`, `notification-service` | Yes | `kafka:9092` | Low | Comma-separated list of Kafka broker hosts and ports. |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`| All Microservices, Gateway | Yes | `http://service-discovery:8761/eureka/` | Low | HTTP endpoint for Eureka service registration and heartbeat. |
| `SERVER_PORT` | All Microservices | Optional | `8080`, `8081`, `8082`, `8083`, `8761` | Low | Internal HTTP listening port for Spring Boot. |
| `JAVA_OPTS` | All Java Containers | Optional | `-Xms128m -Xmx256m ...` | Low | JVM heap, garbage collector, and container options. |
| `GRAFANA_ADMIN_PASSWORD` | `grafana` | Optional | `admin` | Medium | Initial admin UI login password for Grafana. |

---

## 3. Environment Variable Injection Rules
1. **Zero Cleartext Commits**: Production credentials must **never** be committed to Git. The `.env` file is excluded via `.gitignore`.
2. **Fallback Safety**: Development templates provide local non-production defaults (e.g. `${JWT_SECRET:-dGhpcy...}`) to allow instant local boot while requiring explicit overrides in production deployments.

