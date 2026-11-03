# Rule 01: System Architecture and Technology Stack

## Core Invariants
1. **Architecture Style**: Event-Driven Microservices with Bounded Contexts.
2. **Mandatory Stack**:
   - Runtime: Java 21 LTS (OpenJDK / Eclipse Temurin).
   - Core Framework: Spring Boot 3.3.4.
   - Cloud Framework: Spring Cloud 2023.0.3 (Eureka, Spring Cloud Gateway).
   - Database: PostgreSQL 16 Alpine.
   - Database Migration: Flyway 10.x.
   - Messaging Broker: Apache Kafka 3.7+ (KRaft mode). **RabbitMQ is strictly prohibited**.
   - Frontend: React 18, TypeScript 5, Vite, TailwindCSS.
   - Local Orchestration: Docker & Docker Compose.
   - CI/CD: Jenkins Declarative Pipeline.
   - Observability: Prometheus, Grafana, Spring Boot Actuator, Micrometer.
3. **No Third-Party Paid Services**: All infrastructure must run locally via Docker Compose or native containers. Zero SaaS cloud services allowed.

## Microservice Boundaries & Data Isolation
1. **Service Boundary Ownership**:
   - `service-discovery`: Netflix Eureka Server (Port 8761). Instance registration and lookup.
   - `api-gateway`: Spring Cloud Gateway (Port 8080). Edge ingress, JWT authentication filter, route forwarding, rate limiting.
   - `user-service`: Port 8081. User registration, authentication, JWT token generation, user profiles.
   - `task-service`: Port 8082. Task CRUD, status/priority mutations, dashboard metrics, outbox events.
   - `notification-service`: Port 8083. Kafka event consumer, idempotent notification creation, read status.
   - `frontend`: Port 3000. React TypeScript Single Page Application.
2. **Database-per-Service**:
   - `user-service` connects solely to `user_db`.
   - `task-service` connects solely to `task_db`.
   - `notification-service` connects solely to `notification_db`.
   - Direct cross-service database access, cross-database SQL queries, and shared schemas are strictly prohibited.

